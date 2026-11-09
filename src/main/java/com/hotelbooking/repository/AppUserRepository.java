package com.hotelbooking.repository;

import com.hotelbooking.entity.AppUser;
import com.hotelbooking.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

        List<AppUser> findAllByRoles_NameOrderByCreatedAtDesc(RoleName roleName);

        @Query("""
                        select u from AppUser u join u.roles r
                        where r.name = com.hotelbooking.entity.RoleName.CUSTOMER
                            and (:search is null or lower(u.fullName) like lower(concat('%', :search, '%'))
                                     or lower(u.email) like lower(concat('%', :search, '%')))
                        order by u.createdAt desc
                        """)
        List<AppUser> searchCustomers(@Param("search") String search);

        @Query("select count(distinct u) from AppUser u join u.roles r where r.name = com.hotelbooking.entity.RoleName.CUSTOMER")
        long countCustomers();
}