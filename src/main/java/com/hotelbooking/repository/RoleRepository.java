package com.hotelbooking.repository;

import com.hotelbooking.entity.Role;
import com.hotelbooking.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}