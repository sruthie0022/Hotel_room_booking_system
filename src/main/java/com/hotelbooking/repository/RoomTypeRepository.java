package com.hotelbooking.repository;

import com.hotelbooking.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<RoomType> findByNameIgnoreCase(String name);
}