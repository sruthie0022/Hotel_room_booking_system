package com.hotelbooking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record RoomRequest(
        @NotBlank @Size(max = 20) String roomNumber,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 2000) String description,
        @Min(1) int capacity,
        @NotNull @Positive BigDecimal pricePerNight,
        @NotNull Long roomTypeId,
        Set<@NotBlank @Size(max = 80) String> amenities,
        List<@NotBlank @Size(max = 500) String> imageUrls,
        boolean enabled
) {
}