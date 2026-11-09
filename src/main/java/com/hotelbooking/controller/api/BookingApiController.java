package com.hotelbooking.controller.api;

import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.entity.AppUser;
import com.hotelbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingApiController {

    private final BookingService bookingService;

    public BookingApiController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingResponse> myBookings(@AuthenticationPrincipal AppUser user) {
        return bookingService.findForCustomer(user.getId()).stream().map(BookingResponse::from).toList();
    }

    @PostMapping
    public BookingResponse create(@Valid @RequestBody BookingRequest request,
                                  @AuthenticationPrincipal AppUser user) {
        return BookingResponse.from(bookingService.create(user.getId(), request));
    }

    @PostMapping("/{reference}/cancel")
    public BookingResponse cancel(@PathVariable String reference,
                                  @AuthenticationPrincipal AppUser user) {
        return BookingResponse.from(bookingService.cancelForCustomer(user.getId(), reference));
    }
}