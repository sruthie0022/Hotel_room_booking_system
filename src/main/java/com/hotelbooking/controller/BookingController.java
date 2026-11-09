package com.hotelbooking.controller;

import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.entity.AppUser;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.service.BookingService;
import com.hotelbooking.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;

    public BookingController(BookingService bookingService, RoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
    }

    @GetMapping("/booking/new")
    public String newBooking(@RequestParam Long roomId,
                             @RequestParam(required = false) LocalDate checkIn,
                             @RequestParam(required = false) LocalDate checkOut,
                             @RequestParam(defaultValue = "2") int guests,
                             Model model) {
        LocalDate arrival = checkIn == null ? LocalDate.now().plusDays(1) : checkIn;
        LocalDate departure = checkOut == null ? arrival.plusDays(2) : checkOut;
        BookingRequest request = new BookingRequest(roomId, arrival, departure, guests);
        model.addAttribute("bookingRequest", request);
        return showBookingForm(roomId, request, model);
    }

    @PostMapping("/booking/confirm")
    public String confirmBooking(@Valid @ModelAttribute BookingRequest bookingRequest,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal AppUser user,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return showBookingForm(bookingRequest.roomId(), bookingRequest, model);
        }
        try {
            Booking booking = bookingService.create(user.getId(), bookingRequest);
            return "redirect:/booking/confirmation/" + booking.getBookingReference();
        } catch (BusinessException exception) {
            model.addAttribute("bookingError", exception.getMessage());
            return showBookingForm(bookingRequest.roomId(), bookingRequest, model);
        }
    }

    @GetMapping("/booking/confirmation/{reference}")
    public String confirmation(@PathVariable String reference,
                               @AuthenticationPrincipal AppUser user,
                               Model model) {
        model.addAttribute("booking", bookingService.findForCustomerByReference(user.getId(), reference));
        return "booking/confirmation";
    }

    @GetMapping("/my-bookings")
    public String myBookings(@AuthenticationPrincipal AppUser user, Model model) {
        model.addAttribute("bookings", bookingService.findForCustomer(user.getId()));
        return "booking/my-bookings";
    }

    @PostMapping("/my-bookings/{reference}/cancel")
    public String cancel(@PathVariable String reference,
                         @AuthenticationPrincipal AppUser user,
                         RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelForCustomer(user.getId(), reference);
            redirectAttributes.addFlashAttribute("success", "Your booking has been cancelled and refunded.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/my-bookings";
    }

    private String showBookingForm(Long roomId, BookingRequest request, Model model) {
        model.addAttribute("room", roomService.getById(roomId));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("bookingRequest", request);
        return "booking/new";
    }
}