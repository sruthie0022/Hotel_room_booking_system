package com.hotelbooking.service;

import com.hotelbooking.entity.RoomType;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;

    public RoomTypeService(RoomTypeRepository roomTypeRepository, RoomRepository roomRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
    }

    public List<RoomType> findAll() {
        return roomTypeRepository.findAll();
    }

    public RoomType getById(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room type not found."));
    }

    @Transactional
    public RoomType create(String name, String description) {
        String normalizedName = name.trim();
        if (roomTypeRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessException("A room type with that name already exists.");
        }
        return roomTypeRepository.save(new RoomType(normalizedName, cleanDescription(description)));
    }

    @Transactional
    public RoomType update(Long id, String name, String description) {
        RoomType roomType = getById(id);
        String normalizedName = name.trim();
        if (!roomType.getName().equalsIgnoreCase(normalizedName)
                && roomTypeRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessException("A room type with that name already exists.");
        }
        roomType.update(normalizedName, cleanDescription(description));
        return roomType;
    }

    @Transactional
    public void delete(Long id) {
        RoomType roomType = getById(id);
        if (roomRepository.existsByRoomType_Id(id)) {
            throw new BusinessException("A room type assigned to rooms cannot be deleted.");
        }
        roomTypeRepository.delete(roomType);
    }

    private String cleanDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}