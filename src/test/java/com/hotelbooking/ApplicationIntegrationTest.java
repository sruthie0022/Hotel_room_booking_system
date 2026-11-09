package com.hotelbooking;

import com.hotelbooking.dto.RegistrationRequest;
import com.hotelbooking.entity.Room;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.RoomTypeRepository;
import com.hotelbooking.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private AccountService accountService;

    @Test
    void publicPagesAndRoomSearchRender() throws Exception {
        Room room = roomRepository.findAllByEnabledTrueOrderByRoomNumberAsc().get(0);

        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
        mockMvc.perform(get("/rooms")).andExpect(status().isOk()).andExpect(view().name("rooms/list"));
        mockMvc.perform(get("/rooms")
                .param("checkIn", java.time.LocalDate.now().plusDays(5).toString())
                .param("checkOut", java.time.LocalDate.now().plusDays(7).toString())
                .param("guests", "2"))
            .andExpect(status().isOk()).andExpect(view().name("rooms/list"));
        mockMvc.perform(get("/rooms/{id}", room.getId())).andExpect(status().isOk())
            .andExpect(view().name("rooms/details"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString(
                "href=\"/booking/new?roomId=" + room.getId() + "\"")));
        mockMvc.perform(get("/about")).andExpect(status().isOk());
        mockMvc.perform(get("/contact")).andExpect(status().isOk());
        mockMvc.perform(get("/api/rooms")).andExpect(status().isOk());
        mockMvc.perform(get("/api/bookings")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/page-that-does-not-exist")).andExpect(status().isNotFound());
        mockMvc.perform(get("/rooms/999999")).andExpect(status().isNotFound());
    }

    @Test
    void registrationStoresHashedPassword() throws Exception {
        String email = "guest-" + System.nanoTime() + "@example.test";
        var user = accountService.register(new RegistrationRequest(
                "Test Guest", email, "+1 503 555 0100", "HouseStay123!"));

        assertNotNull(user.getId());
        assertNotEquals("HouseStay123!", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2a$"));

        mockMvc.perform(post("/login").with(csrf())
                .param("username", email)
                .param("password", "HouseStay123!"))
            .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "New Guest")
                        .param("email", "new-" + System.nanoTime() + "@example.test")
                        .param("phoneNumber", "+1 503 555 0101")
                        .param("password", "StrongPassword8!"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void adminRoutesRequireAdminRole() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/admin").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("guest").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void reservationLoginRedirectRespectsForwardedCodespacesHost() throws Exception {
        Room room = roomRepository.findAllByEnabledTrueOrderByRoomNumberAsc().get(0);

        mockMvc.perform(get("/booking/new")
                        .param("roomId", room.getId().toString())
                        .header("X-Forwarded-Host", "hotel-demo.example.dev")
                        .header("X-Forwarded-Proto", "https")
                        .header("X-Forwarded-Port", "443"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "https://hotel-demo.example.dev/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDashboardAndManagementPagesRender() throws Exception {
        mockMvc.perform(get("/admin")).andExpect(status().isOk()).andExpect(view().name("admin/dashboard"));
        mockMvc.perform(get("/admin/rooms")).andExpect(status().isOk()).andExpect(view().name("admin/rooms"));
        mockMvc.perform(get("/admin/rooms/new")).andExpect(status().isOk()).andExpect(view().name("admin/room-form"));
        mockMvc.perform(get("/admin/room-types")).andExpect(status().isOk()).andExpect(view().name("admin/room-types"));
        mockMvc.perform(get("/admin/room-types/new")).andExpect(status().isOk()).andExpect(view().name("admin/room-type-form"));
        mockMvc.perform(get("/admin/bookings")).andExpect(status().isOk()).andExpect(view().name("admin/bookings"));
        mockMvc.perform(get("/admin/customers")).andExpect(status().isOk()).andExpect(view().name("admin/customers"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateARoom() throws Exception {
        Long roomTypeId = roomRepository.findAllByEnabledTrueOrderByRoomNumberAsc().get(0).getRoomType().getId();
        String roomNumber = "T" + Long.toString(System.nanoTime(), 36).substring(0, 7).toUpperCase();

        mockMvc.perform(post("/admin/rooms").with(csrf())
                        .param("roomNumber", roomNumber)
                        .param("name", "Test Garden Room")
                        .param("description", "A test room created by the integration suite.")
                        .param("capacity", "2")
                        .param("pricePerNight", "199.00")
                        .param("roomTypeId", roomTypeId.toString())
                        .param("amenitiesText", "King bed, Garden view")
                        .param("imageUrlsText", "https://example.test/room.jpg")
                        .param("enabled", "true"))
                .andExpect(status().is3xxRedirection());

        assertTrue(roomRepository.existsByRoomNumberIgnoreCase(roomNumber));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateARoomType() throws Exception {
        String roomTypeName = "Test Type " + Long.toString(System.nanoTime(), 36);

        mockMvc.perform(post("/admin/room-types").with(csrf())
                        .param("name", roomTypeName)
                        .param("description", "Created by the integration suite."))
                .andExpect(status().is3xxRedirection());

        assertTrue(roomTypeRepository.existsByNameIgnoreCase(roomTypeName));
    }

        @Test
        void customerCanCompleteAndManageAStay() throws Exception {
        var customer = accountService.register(new RegistrationRequest(
            "Stay Guest", "stay-" + System.nanoTime() + "@example.test",
            "+1 503 555 0142", "HouseStay123!"));
        Room room = roomRepository.findAllByEnabledTrueOrderByRoomNumberAsc().get(0);
        String checkIn = java.time.LocalDate.now().plusDays(14).toString();
        String checkOut = java.time.LocalDate.now().plusDays(16).toString();

        mockMvc.perform(get("/booking/new").with(user(customer))
                .param("roomId", room.getId().toString()))
            .andExpect(status().isOk()).andExpect(view().name("booking/new"));

        MvcResult bookingResult = mockMvc.perform(post("/booking/confirm").with(user(customer)).with(csrf())
                .param("roomId", room.getId().toString())
                .param("checkIn", checkIn)
                .param("checkOut", checkOut)
                .param("guests", "2"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        String confirmationUrl = bookingResult.getResponse().getRedirectedUrl();

        mockMvc.perform(get(confirmationUrl).with(user(customer)))
            .andExpect(status().isOk()).andExpect(view().name("booking/confirmation"));
        mockMvc.perform(get("/my-bookings").with(user(customer)))
            .andExpect(status().isOk()).andExpect(view().name("booking/my-bookings"));
        }
}