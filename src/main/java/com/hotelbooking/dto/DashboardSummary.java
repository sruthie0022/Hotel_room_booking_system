package com.hotelbooking.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardSummary(
        long totalBookings,
        long totalRooms,
        long availableRooms,
        long occupiedRooms,
        long registeredCustomers,
        BigDecimal totalRevenue,
        List<RecentBooking> recentBookings,
        Map<String, Long> bookingStatistics
) {
    public record RecentBooking(String reference, String customer, String room, String dates,
                                String status, BigDecimal total) {
    }
}