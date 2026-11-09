package com.hotelbooking.service;

import com.hotelbooking.dto.DashboardSummary;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.repository.AppUserRepository;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.RoomRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final AppUserRepository appUserRepository;

    public DashboardService(BookingRepository bookingRepository, RoomRepository roomRepository,
                            AppUserRepository appUserRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.appUserRepository = appUserRepository;
    }

    public DashboardSummary summary() {
        long occupied = bookingRepository.countOccupiedRooms(LocalDate.now(),
                Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED));
        long activeRooms = roomRepository.countByEnabledTrue();
        var recent = bookingRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 6)).stream()
                .map(booking -> new DashboardSummary.RecentBooking(booking.getBookingReference(),
                        booking.getUser().getFullName(), booking.getRoom().getName(),
                        booking.getCheckIn().format(DateTimeFormatter.ofPattern("MMM d")) + " - "
                                + booking.getCheckOut().format(DateTimeFormatter.ofPattern("MMM d")),
                        booking.getStatus().name(), booking.getTotalPrice()))
                .toList();
        Map<String, Long> statistics = new LinkedHashMap<>();
        for (BookingStatus status : BookingStatus.values()) {
            statistics.put(status.name(), 0L);
        }
        for (Object[] row : bookingRepository.countByStatusGrouped()) {
            statistics.put(((BookingStatus) row[0]).name(), (Long) row[1]);
        }
        return new DashboardSummary(bookingRepository.count(), roomRepository.count(),
                activeRooms - occupied, occupied, appUserRepository.countCustomers(),
                bookingRepository.totalRevenue(), recent, statistics);
    }
}