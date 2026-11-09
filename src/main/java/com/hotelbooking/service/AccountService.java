package com.hotelbooking.service;

import com.hotelbooking.dto.RegistrationRequest;
import com.hotelbooking.entity.AppUser;
import com.hotelbooking.entity.RoleName;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.repository.AppUserRepository;
import com.hotelbooking.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AccountService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AppUserRepository appUserRepository, RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegistrationRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("An account with that email already exists.");
        }
        var customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("Customer role has not been initialized."));
        AppUser user = new AppUser(request.fullName().trim(), email, request.phoneNumber().trim(),
                passwordEncoder.encode(request.password()));
        user.addRole(customerRole);
        return appUserRepository.save(user);
    }
}