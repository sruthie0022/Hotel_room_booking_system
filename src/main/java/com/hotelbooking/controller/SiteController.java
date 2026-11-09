package com.hotelbooking.controller;

import com.hotelbooking.entity.Room;
import com.hotelbooking.repository.RoomTypeRepository;
import com.hotelbooking.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
public class SiteController {

    private final RoomService roomService;
    private final RoomTypeRepository roomTypeRepository;

    public SiteController(RoomService roomService, RoomTypeRepository roomTypeRepository) {
        this.roomService = roomService;
        this.roomTypeRepository = roomTypeRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredRooms", roomService.findAllEnabled().stream().limit(3).toList());
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        model.addAttribute("today", LocalDate.now());
        return "home";
    }

    @GetMapping("/rooms")
    public String rooms(@RequestParam(required = false) LocalDate checkIn,
                        @RequestParam(required = false) LocalDate checkOut,
                        @RequestParam(defaultValue = "2") int guests,
                        @RequestParam(required = false) Long roomTypeId,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @RequestParam(defaultValue = "recommended") String sort,
                        Model model) {
        boolean searchingDates = checkIn != null && checkOut != null;
        List<Room> availableRooms = searchingDates
                ? roomService.findAvailable(checkIn, checkOut, guests, roomTypeId)
                : roomService.findAllEnabled();
        var filteredRooms = availableRooms.stream()
                .filter(room -> roomTypeId == null || room.getRoomType().getId().equals(roomTypeId))
                .filter(room -> maxPrice == null || room.getPricePerNight().compareTo(maxPrice) <= 0);
        List<Room> results = switch (sort) {
            case "price-low" -> filteredRooms.sorted(Comparator.comparing(Room::getPricePerNight)).toList();
            case "price-high" -> filteredRooms.sorted(Comparator.comparing(Room::getPricePerNight).reversed()).toList();
            case "capacity" -> filteredRooms.sorted(Comparator.comparing(Room::getCapacity).reversed()).toList();
            default -> filteredRooms.toList();
        };
        model.addAttribute("rooms", results);
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("guests", guests);
        model.addAttribute("roomTypeId", roomTypeId);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("searchingDates", searchingDates);
        return "rooms/list";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetails(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomService.getById(id));
        model.addAttribute("today", LocalDate.now());
        return "rooms/details";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}