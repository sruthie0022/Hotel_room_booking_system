package com.hotelbooking.controller;

import com.hotelbooking.dto.AdminRoomForm;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.repository.RoomTypeRepository;
import com.hotelbooking.service.BookingService;
import com.hotelbooking.service.CustomerManagementService;
import com.hotelbooking.service.DashboardService;
import com.hotelbooking.service.RoomImageStorageService;
import com.hotelbooking.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final RoomService roomService;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingService bookingService;
    private final CustomerManagementService customerService;
    private final RoomImageStorageService imageStorageService;

    public AdminController(DashboardService dashboardService, RoomService roomService,
                           RoomTypeRepository roomTypeRepository, BookingService bookingService,
                           CustomerManagementService customerService, RoomImageStorageService imageStorageService) {
        this.dashboardService = dashboardService;
        this.roomService = roomService;
        this.roomTypeRepository = roomTypeRepository;
        this.bookingService = bookingService;
        this.customerService = customerService;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("summary", dashboardService.summary());
        return "admin/dashboard";
    }

    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("rooms", roomService.findAllForAdmin());
        return "admin/rooms";
    }

    @GetMapping("/rooms/new")
    public String newRoom(Model model) {
        model.addAttribute("roomForm", new AdminRoomForm());
        addRoomTypes(model);
        model.addAttribute("editing", false);
        return "admin/room-form";
    }

    @PostMapping("/rooms")
    public String createRoom(@Valid AdminRoomForm roomForm, BindingResult bindingResult,
                             @RequestParam(required = false) MultipartFile image,
                             Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return showRoomForm(roomForm, model, false);
        }
        try {
            appendUploadedImage(roomForm, image);
            roomService.create(roomForm.toRequest());
            redirectAttributes.addFlashAttribute("success", "Room added to your inventory.");
            return "redirect:/admin/rooms";
        } catch (BusinessException exception) {
            bindingResult.reject("room", exception.getMessage());
            return showRoomForm(roomForm, model, false);
        }
    }

    @GetMapping("/rooms/{id}/edit")
    public String editRoom(@PathVariable Long id, Model model) {
        model.addAttribute("roomForm", new AdminRoomForm(roomService.getForAdmin(id)));
        model.addAttribute("roomId", id);
        addRoomTypes(model);
        model.addAttribute("editing", true);
        return "admin/room-form";
    }

    @PostMapping("/rooms/{id}")
    public String updateRoom(@PathVariable Long id, @Valid AdminRoomForm roomForm,
                             BindingResult bindingResult,
                             @RequestParam(required = false) MultipartFile image,
                             Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roomId", id);
            return showRoomForm(roomForm, model, true);
        }
        try {
            appendUploadedImage(roomForm, image);
            roomService.update(id, roomForm.toRequest());
            redirectAttributes.addFlashAttribute("success", "Room details have been updated.");
            return "redirect:/admin/rooms";
        } catch (BusinessException exception) {
            bindingResult.reject("room", exception.getMessage());
            model.addAttribute("roomId", id);
            return showRoomForm(roomForm, model, true);
        }
    }

    @PostMapping("/rooms/{id}/delete")
    public String disableRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        roomService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Room is no longer listed for booking.");
        return "redirect:/admin/rooms";
    }

    @GetMapping("/bookings")
    public String bookings(@RequestParam(required = false) BookingStatus status,
                           @RequestParam(required = false) LocalDate fromDate,
                           @RequestParam(required = false) LocalDate toDate,
                           @RequestParam(required = false) String search,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        Page<?> results = bookingService.searchForAdmin(status, fromDate, toDate, search, page);
        model.addAttribute("bookings", results);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("search", search);
        return "admin/bookings";
    }

    @PostMapping("/bookings/{reference}/status")
    public String updateBookingStatus(@PathVariable String reference,
                                     @RequestParam BookingStatus status,
                                     RedirectAttributes redirectAttributes) {
        try {
            bookingService.updateStatusByAdmin(reference, status);
            redirectAttributes.addFlashAttribute("success", "Booking status updated.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/customers")
    public String customers(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("customers", customerService.search(search));
        model.addAttribute("search", search);
        return "admin/customers";
    }

    @GetMapping("/customers/{id}")
    public String customerDetails(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.getCustomer(id));
        model.addAttribute("bookings", customerService.bookingHistory(id));
        return "admin/customer-details";
    }

    @PostMapping("/customers/{id}/enabled")
    public String setCustomerEnabled(@PathVariable Long id, @RequestParam boolean enabled,
                                     RedirectAttributes redirectAttributes) {
        customerService.setEnabled(id, enabled);
        redirectAttributes.addFlashAttribute("success", enabled ? "Customer account enabled." : "Customer account disabled.");
        return "redirect:/admin/customers";
    }

    private String showRoomForm(AdminRoomForm form, Model model, boolean editing) {
        model.addAttribute("roomForm", form);
        addRoomTypes(model);
        model.addAttribute("editing", editing);
        return "admin/room-form";
    }

    private void addRoomTypes(Model model) {
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
    }

    private void appendUploadedImage(AdminRoomForm form, MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            String savedImage = imageStorageService.store(image);
            String existing = form.getImageUrlsText().isBlank() ? "" : form.getImageUrlsText() + System.lineSeparator();
            form.setImageUrlsText(existing + savedImage);
        }
    }
}