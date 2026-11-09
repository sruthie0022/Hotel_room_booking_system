package com.hotelbooking.dto;

import com.hotelbooking.entity.Room;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record RoomResponse(Long id, String roomNumber, String name, String description, int capacity,
                           BigDecimal pricePerNight, String roomType, Set<String> amenities,
                           List<String> imageUrls, boolean enabled) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getRoomNumber(), room.getName(), room.getDescription(),
                room.getCapacity(), room.getPricePerNight(), room.getRoomType().getName(),
                Set.copyOf(room.getAmenities()), List.copyOf(room.getImageUrls()), room.isEnabled());
    }
}