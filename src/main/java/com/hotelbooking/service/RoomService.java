package com.hotelbooking.service;

import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.RoomType;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository roomRepository, RoomTypeRepository roomTypeRepository,
                       BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Room> findAllEnabled() {
        return roomRepository.findAllByEnabledTrueOrderByRoomNumberAsc();
    }

    public List<Room> findAllForAdmin() {
        return roomRepository.findAllByOrderByRoomNumberAsc();
    }

    public List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut, int guests, Long roomTypeId) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new BusinessException("Choose a check-out date after check-in.");
        }
        if (guests < 1) {
            throw new BusinessException("At least one guest is required.");
        }
        return roomRepository.findAvailable(checkIn, checkOut, guests, roomTypeId,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));
    }

    public Room getById(Long id) {
        return roomRepository.findWithDetailsById(id).filter(Room::isEnabled)
                .orElseThrow(() -> new NotFoundException("Room not found."));
    }

    public Room getForAdmin(Long id) {
        return roomRepository.findAdminDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Room not found."));
    }

    @Transactional
    public Room create(RoomRequest request) {
        if (roomRepository.existsByRoomNumberIgnoreCase(request.roomNumber().trim())) {
            throw new BusinessException("That room number is already in use.");
        }
        RoomType type = getRoomType(request.roomTypeId());
        return roomRepository.save(new Room(request.roomNumber().trim(), request.name().trim(),
                request.description().trim(), request.capacity(), request.pricePerNight(), type,
                safeSet(request.amenities()), safeList(request.imageUrls())));
    }

    @Transactional
    public Room update(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found."));
        if (!room.getRoomNumber().equalsIgnoreCase(request.roomNumber().trim())
                && roomRepository.existsByRoomNumberIgnoreCase(request.roomNumber().trim())) {
            throw new BusinessException("That room number is already in use.");
        }
        room.update(request.name().trim(), request.description().trim(), request.capacity(),
                request.pricePerNight(), getRoomType(request.roomTypeId()), safeSet(request.amenities()),
                safeList(request.imageUrls()), request.enabled());
        return room;
    }

    @Transactional
    public void delete(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found."));
        room.update(room.getName(), room.getDescription(), room.getCapacity(), room.getPricePerNight(),
                room.getRoomType(), room.getAmenities(), room.getImageUrls(), false);
    }

    private RoomType getRoomType(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room type not found."));
    }

    private Set<String> safeSet(Set<String> values) {
        return values == null ? Set.of() : values;
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }
}