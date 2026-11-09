package com.hotelbooking.controller.api;

import com.hotelbooking.dto.RoomResponse;
import com.hotelbooking.repository.RoomTypeRepository;
import com.hotelbooking.service.RoomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomApiController {

    private final RoomService roomService;
    private final RoomTypeRepository roomTypeRepository;

    public RoomApiController(RoomService roomService, RoomTypeRepository roomTypeRepository) {
        this.roomService = roomService;
        this.roomTypeRepository = roomTypeRepository;
    }

    @GetMapping
    public List<RoomResponse> rooms() {
        return roomService.findAllEnabled().stream().map(RoomResponse::from).toList();
    }

    @GetMapping("/available")
    public List<RoomResponse> available(@RequestParam LocalDate checkIn,
                                        @RequestParam LocalDate checkOut,
                                        @RequestParam(defaultValue = "1") int guests,
                                        @RequestParam(required = false) Long roomTypeId) {
        return roomService.findAvailable(checkIn, checkOut, guests, roomTypeId).stream()
                .map(RoomResponse::from).toList();
    }

    @GetMapping("/types")
    public List<String> roomTypes() {
        return roomTypeRepository.findAll().stream().map(type -> type.getName()).toList();
    }
}