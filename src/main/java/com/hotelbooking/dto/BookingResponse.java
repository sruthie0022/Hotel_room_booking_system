package com.hotelbooking.dto;

import com.hotelbooking.entity.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingResponse(String reference, Long roomId, String roomName, LocalDate checkIn,
                              LocalDate checkOut, int guests, String status, BigDecimal totalPrice,
                              String paymentReference) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(booking.getBookingReference(), booking.getRoom().getId(),
                booking.getRoom().getName(), booking.getCheckIn(), booking.getCheckOut(),
                booking.getGuests(), booking.getStatus().name(), booking.getTotalPrice(),
                booking.getPayment() == null ? null : booking.getPayment().getPaymentReference());
    }
}