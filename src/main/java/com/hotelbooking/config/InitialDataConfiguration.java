package com.hotelbooking.config;

import com.hotelbooking.entity.AppUser;
import com.hotelbooking.entity.Role;
import com.hotelbooking.entity.RoleName;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.RoomType;
import com.hotelbooking.repository.AppUserRepository;
import com.hotelbooking.repository.RoleRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Configuration
public class InitialDataConfiguration {

    @Bean
    ApplicationRunner initializeHotelData(RoleRepository roleRepository,
                                          RoomTypeRepository roomTypeRepository,
                                          RoomRepository roomRepository,
                                          AppUserRepository appUserRepository,
                                          PasswordEncoder passwordEncoder,
                                          @Value("${hotel.admin.email:}") String adminEmail,
                                          @Value("${hotel.admin.password:}") String adminPassword) {
        return args -> {
            Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                    .orElseGet(() -> roleRepository.save(new Role(RoleName.CUSTOMER)));
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN)));

            if (!adminEmail.isBlank() && !adminPassword.isBlank()
                    && !appUserRepository.existsByEmailIgnoreCase(adminEmail)) {
                AppUser admin = new AppUser("Hotel Administrator", adminEmail.trim().toLowerCase(),
                        "0000000000", passwordEncoder.encode(adminPassword));
                admin.addRole(adminRole);
                appUserRepository.save(admin);
            }

            if (roomTypeRepository.count() == 0) {
                roomTypeRepository.saveAll(List.of(
                        new RoomType("Deluxe", "Elevated comfort with thoughtful details."),
                        new RoomType("Suite", "Generous living space with a separate lounge."),
                        new RoomType("Garden View", "A quiet retreat overlooking the courtyard.")));
            }

            if (roomRepository.count() == 0) {
                RoomType deluxe = roomTypeRepository.findByNameIgnoreCase("Deluxe").orElseThrow();
                RoomType suite = roomTypeRepository.findByNameIgnoreCase("Suite").orElseThrow();
                RoomType garden = roomTypeRepository.findByNameIgnoreCase("Garden View").orElseThrow();
                roomRepository.saveAll(List.of(
                        new Room("101", "The City Deluxe", "A serene, light-filled room with handcrafted finishes and a king bed.",
                                2, new BigDecimal("245.00"), deluxe,
                                Set.of("King bed", "Rain shower", "Fast Wi-Fi", "Breakfast"),
                                List.of(
                                        "https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1400&q=85",
                                        "https://images.unsplash.com/photo-1616594039964-ae9021a400a0?auto=format&fit=crop&w=1400&q=85",
                                        "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=1400&q=85")),
                        new Room("204", "The Garden Suite", "A spacious suite with a separate sitting area and leafy courtyard views.",
                                3, new BigDecimal("390.00"), suite,
                                Set.of("King bed", "Lounge", "Soaking tub", "Fast Wi-Fi"),
                                List.of(
                                        "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=1400&q=85",
                                        "https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=1400&q=85",
                                        "https://images.unsplash.com/photo-1616594039964-ae9021a400a0?auto=format&fit=crop&w=1400&q=85")),
                        new Room("312", "The Conservatory Room", "A calm sanctuary framed by the hotel's private garden.",
                                2, new BigDecimal("285.00"), garden,
                                Set.of("Queen bed", "Garden view", "Rain shower", "Breakfast"),
                                List.of(
                                        "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=1400&q=85",
                                        "https://images.unsplash.com/photo-1616594039964-ae9021a400a0?auto=format&fit=crop&w=1400&q=85",
                                        "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=1400&q=85"))));
            }
        };
    }
}