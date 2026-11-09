package com.hotelbooking.dto;

import com.hotelbooking.entity.Room;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class AdminRoomForm {

    @NotBlank @Size(max = 20)
    private String roomNumber;
    @NotBlank @Size(max = 100)
    private String name;
    @NotBlank @Size(max = 2000)
    private String description;
    @Min(1)
    private int capacity = 2;
    @NotNull @Positive
    private BigDecimal pricePerNight;
    @NotNull
    private Long roomTypeId;
    private String amenitiesText = "";
    private String imageUrlsText = "";
    private boolean enabled = true;

    public AdminRoomForm() {
    }

    public AdminRoomForm(Room room) {
        roomNumber = room.getRoomNumber();
        name = room.getName();
        description = room.getDescription();
        capacity = room.getCapacity();
        pricePerNight = room.getPricePerNight();
        roomTypeId = room.getRoomType().getId();
        amenitiesText = String.join(", ", room.getAmenities());
        imageUrlsText = String.join(System.lineSeparator(), room.getImageUrls());
        enabled = room.isEnabled();
    }

    public RoomRequest toRequest() {
        var amenities = Arrays.stream(amenitiesText.split("[,\\n]"))
                .map(String::trim).filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<String> imageUrls = Arrays.stream(imageUrlsText.split("\\R"))
                .map(String::trim).filter(value -> !value.isBlank()).toList();
        return new RoomRequest(roomNumber, name, description, capacity, pricePerNight,
                roomTypeId, amenities, imageUrls, enabled);
    }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }
    public String getAmenitiesText() { return amenitiesText; }
    public void setAmenitiesText(String amenitiesText) { this.amenitiesText = amenitiesText == null ? "" : amenitiesText; }
    public String getImageUrlsText() { return imageUrlsText; }
    public void setImageUrlsText(String imageUrlsText) { this.imageUrlsText = imageUrlsText == null ? "" : imageUrlsText; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}