package com.hotelbooking;

import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.dto.RegistrationRequest;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.entity.PaymentStatus;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.service.AccountService;
import com.hotelbooking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BookingServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    @Transactional
    void bookingCalculatesPriceBlocksOverlapsAndRefundsCancellation() {
        var user = accountService.register(new RegistrationRequest(
                "Booking Guest", "booking-" + System.nanoTime() + "@example.test",
                "+1 503 555 0140", "HouseStay123!"));
        var room = roomRepository.findAllByEnabledTrueOrderByRoomNumberAsc().get(0);
        LocalDate checkIn = LocalDate.now().plusDays(8);
        LocalDate checkOut = checkIn.plusDays(3);
        var request = new BookingRequest(room.getId(), checkIn, checkOut, 2);

        var booking = bookingService.create(user.getId(), request);

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(room.getPricePerNight().multiply(java.math.BigDecimal.valueOf(ChronoUnit.DAYS.between(checkIn, checkOut))),
                booking.getTotalPrice());
        assertEquals(PaymentStatus.SUCCEEDED, booking.getPayment().getStatus());
        assertThrows(BusinessException.class, () -> bookingService.create(user.getId(), request));

        var cancelled = bookingService.cancelForCustomer(user.getId(), booking.getBookingReference());
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
        assertEquals(PaymentStatus.REFUNDED, cancelled.getPayment().getStatus());
        assertTrue(bookingService.findForCustomer(user.getId()).contains(cancelled));
    }

    @Test
    @Transactional
    void bookingRejectsInvalidDatesAndGuestCapacity() {
        var user = accountService.register(new RegistrationRequest(
                "Capacity Guest", "capacity-" + System.nanoTime() + "@example.test",
                "+1 503 555 0141", "HouseStay123!"));
        var room = roomRepository.findAllByEnabledTrueOrderByRoomNumberAsc().get(0);
        LocalDate checkIn = LocalDate.now().plusDays(10);

        assertThrows(BusinessException.class, () -> bookingService.create(user.getId(),
            new BookingRequest(room.getId(), checkIn, checkIn.plusDays(1), room.getCapacity() + 1)));
        assertThrows(BusinessException.class, () -> bookingService.create(user.getId(),
            new BookingRequest(room.getId(), checkIn, checkIn, 1)));
    }
}