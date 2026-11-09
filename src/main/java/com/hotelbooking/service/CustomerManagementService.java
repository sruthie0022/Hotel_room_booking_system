package com.hotelbooking.service;

import com.hotelbooking.entity.AppUser;
import com.hotelbooking.entity.RoleName;
import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.repository.AppUserRepository;
import com.hotelbooking.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerManagementService {

    private final AppUserRepository appUserRepository;
    private final BookingRepository bookingRepository;

    public CustomerManagementService(AppUserRepository appUserRepository, BookingRepository bookingRepository) {
        this.appUserRepository = appUserRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<AppUser> search(String search) {
        String query = search == null || search.isBlank() ? null : search.trim();
        return appUserRepository.searchCustomers(query);
    }

    public AppUser getCustomer(Long id) {
        AppUser customer = appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found."));
        if (customer.getRoles().stream().noneMatch(role -> role.getName() == RoleName.CUSTOMER)) {
            throw new NotFoundException("Customer not found.");
        }
        return customer;
    }

    public List<com.hotelbooking.entity.Booking> bookingHistory(Long id) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(getCustomer(id));
    }

    @Transactional
    public AppUser setEnabled(Long id, boolean enabled) {
        AppUser customer = getCustomer(id);
        customer.setEnabled(enabled);
        return customer;
    }
}