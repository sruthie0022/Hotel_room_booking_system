package com.hotelbooking.repository;

import com.hotelbooking.entity.AppUser;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

        @EntityGraph(attributePaths = {"room", "room.roomType", "room.amenities", "room.imageUrls", "payment"})
        List<Booking> findByUserOrderByCreatedAtDesc(AppUser user);

        @EntityGraph(attributePaths = {"user", "room", "room.roomType", "room.amenities", "room.imageUrls", "payment"})
    Optional<Booking> findByBookingReference(String bookingReference);

        @EntityGraph(attributePaths = {"user", "room", "room.roomType", "payment"})
        Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            select b from Booking b
            where (:status is null or b.status = :status)
              and (:fromDate is null or b.checkOut >= :fromDate)
              and (:toDate is null or b.checkIn <= :toDate)
              and (:search is null or lower(b.bookingReference) like lower(concat('%', :search, '%'))
                   or lower(b.user.fullName) like lower(concat('%', :search, '%'))
                   or lower(b.user.email) like lower(concat('%', :search, '%')))
            order by b.createdAt desc
            """)
        @EntityGraph(attributePaths = {"user", "room", "room.roomType", "payment"})
        Page<Booking> search(@Param("status") BookingStatus status,
                         @Param("fromDate") LocalDate fromDate,
                         @Param("toDate") LocalDate toDate,
                         @Param("search") String search,
                         Pageable pageable);

    @Query("""
            select case when count(b) > 0 then true else false end
            from Booking b where b.room.id = :roomId
              and b.status in :blockingStatuses
              and b.checkIn < :checkOut and b.checkOut > :checkIn
            """)
    boolean existsOverlappingBooking(@Param("roomId") Long roomId,
                                     @Param("checkIn") LocalDate checkIn,
                                     @Param("checkOut") LocalDate checkOut,
                                     @Param("blockingStatuses") Collection<BookingStatus> blockingStatuses);

    long countByStatus(BookingStatus status);

    @Query("""
            select count(distinct b.room.id) from Booking b
            where b.status in :blockingStatuses and b.checkIn <= :today and b.checkOut > :today
            """)
    long countOccupiedRooms(@Param("today") LocalDate today,
                            @Param("blockingStatuses") Collection<BookingStatus> blockingStatuses);

    long countByCreatedAtAfter(java.time.Instant after);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = com.hotelbooking.entity.PaymentStatus.SUCCEEDED")
    java.math.BigDecimal totalRevenue();

    @Query("select b.status, count(b) from Booking b group by b.status")
    List<Object[]> countByStatusGrouped();
}