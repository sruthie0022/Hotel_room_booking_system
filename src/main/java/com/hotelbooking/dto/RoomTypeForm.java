package com.hotelbooking.dto;

import com.hotelbooking.entity.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RoomTypeForm {

    @NotBlank @Size(max = 60)
    private String name;
    @Size(max = 500)
    private String description;

    public RoomTypeForm() {
    }

    public RoomTypeForm(RoomType roomType) {
        name = roomType.getName();
        description = roomType.getDescription();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}