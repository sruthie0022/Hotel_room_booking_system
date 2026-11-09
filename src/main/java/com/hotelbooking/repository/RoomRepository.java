package com.hotelbooking.repository;

import com.hotelbooking.entity.BookingStatus;
import com.hotelbooking.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @EntityGraph(attributePaths = {"roomType", "amenities", "imageUrls"})
    List<Room> findAllByEnabledTrueOrderByRoomNumberAsc();

    @EntityGraph(attributePaths = {"roomType", "amenities", "imageUrls"})
    List<Room> findAllByOrderByRoomNumberAsc();

  long countByEnabledTrue();

  boolean existsByRoomType_Id(Long roomTypeId);

    @EntityGraph(attributePaths = {"roomType", "amenities", "imageUrls"})
    Optional<Room> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"roomType", "amenities", "imageUrls"})
    Optional<Room> findAdminDetailsById(Long id);

    boolean existsByRoomNumberIgnoreCase(String roomNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"roomType", "amenities", "imageUrls"})
    @Query("""
            select distinct r from Room r
            where r.enabled = true and r.capacity >= :guests
              and (:roomTypeId is null or r.roomType.id = :roomTypeId)
              and not exists (
                select b.id from Booking b
                where b.room = r and b.status in :blockingStatuses
                  and b.checkIn < :checkOut and b.checkOut > :checkIn
              )
            order by r.pricePerNight asc
            """)
    List<Room> findAvailable(@Param("checkIn") LocalDate checkIn,
                             @Param("checkOut") LocalDate checkOut,
                             @Param("guests") int guests,
                             @Param("roomTypeId") Long roomTypeId,
                             @Param("blockingStatuses") Collection<BookingStatus> blockingStatuses);
}