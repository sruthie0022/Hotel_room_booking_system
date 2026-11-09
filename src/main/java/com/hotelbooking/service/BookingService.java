package com.hotelbooking.service;

import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.entity.AppUser;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.entity.PaymentStatus;
import com.hotelbooking.entity.Room;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.repository.AppUserRepository;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private static final List<BookingStatus> BLOCKING_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final AppUserRepository appUserRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
                          AppUserRepository appUserRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public Booking create(Long userId, BookingRequest request) {
        validateDates(request.checkIn(), request.checkOut());
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Customer account not found."));
        Room room = roomRepository.findByIdForUpdate(request.roomId())
                .filter(Room::isEnabled)
                .orElseThrow(() -> new NotFoundException("Room not found."));
        if (request.guests() > room.getCapacity()) {
            throw new BusinessException("This room cannot accommodate that many guests.");
        }
        if (bookingRepository.existsOverlappingBooking(room.getId(), request.checkIn(), request.checkOut(),
                BLOCKING_STATUSES)) {
            throw new BusinessException("This room is no longer available for those dates.");
        }

        long nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        BigDecimal total = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        Booking booking = bookingRepository.save(new Booking(user, room, request.checkIn(), request.checkOut(),
                request.guests(), room.getPricePerNight(), total));

        Payment payment = new Payment(booking, total);
        payment.markSucceeded();
        booking.setPayment(payment);
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    public List<Booking> findForCustomer(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Customer account not found."));
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Booking findForCustomerByReference(Long userId, String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new NotFoundException("Booking not found."));
        if (!booking.getUser().getId().equals(userId)) {
            throw new NotFoundException("Booking not found.");
        }
        return booking;
    }

    public Page<Booking> searchForAdmin(BookingStatus status, LocalDate fromDate, LocalDate toDate,
                                        String search, int page) {
        String query = search == null || search.isBlank() ? null : search.trim();
        return bookingRepository.search(status, fromDate, toDate, query, PageRequest.of(Math.max(page, 0), 20));
    }

    @Transactional
    public Booking cancelForCustomer(Long userId, String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new NotFoundException("Booking not found."));
        if (!booking.getUser().getId().equals(userId)) {
            throw new NotFoundException("Booking not found.");
        }
        cancel(booking);
        return booking;
    }

    @Transactional
    public Booking cancelByAdmin(String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new NotFoundException("Booking not found."));
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessException("This booking can no longer be cancelled.");
        }
        markCancelled(booking);
        return booking;
    }

    @Transactional
    public Booking updateStatusByAdmin(String reference, BookingStatus status) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new NotFoundException("Booking not found."));
        if (status == BookingStatus.CANCELLED) {
            return cancelByAdmin(reference);
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("A cancelled booking cannot be reopened.");
        }
        booking.setStatus(status);
        return booking;
    }

    private void cancel(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("This booking can no longer be cancelled.");
        }
        if (!LocalDate.now().isBefore(booking.getCheckIn().minusDays(1))) {
            throw new BusinessException("Bookings can only be cancelled at least 24 hours before check-in.");
        }
        markCancelled(booking);
    }

    private void markCancelled(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
        if (booking.getPayment() != null && booking.getPayment().getStatus() == PaymentStatus.SUCCEEDED) {
            booking.getPayment().markRefunded();
        }
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkIn.isBefore(LocalDate.now())) {
            throw new BusinessException("Check-in must be today or later.");
        }
        if (checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new BusinessException("Check-out must be after check-in.");
        }
    }
}