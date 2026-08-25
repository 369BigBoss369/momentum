package com.momentum.user.web;

import com.momentum.config.TestConfig;
import com.momentum.user.dto.AdminStats;
import com.momentum.user.dto.ModerationQueue;
import com.momentum.user.model.User;
import com.momentum.user.model.enums.UserRole;
import com.momentum.user.service.AdminService;
import com.momentum.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(TestConfig.class)
class AdminControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AdminService adminService;

    private UUID adminId;
    private User adminUser;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        adminUser = User.builder().id(adminId).username("admin").role(UserRole.ADMIN).build();
        when(userService.getCurrentUser(any())).thenReturn(adminUser);
        when(userService.countActiveAdmins()).thenReturn(1L);
    }

    private ModerationQueue emptyModerationQueue() {
        return ModerationQueue.builder()
                .pendingProducts(Collections.emptyList())
                .pendingCompositeFoods(Collections.emptyList())
                .pendingRecipes(Collections.emptyList())
                .pendingExercises(Collections.emptyList())
                .pendingWorkouts(Collections.emptyList())
                .pendingPlans(Collections.emptyList())
                .usernamesByOwnerId(Collections.emptyMap())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listUsers_ShouldReturnUsersView() throws Exception {
        when(userService.searchUsers(any(), any(), any())).thenReturn(Arrays.asList(adminUser));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("currentUserId", adminId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_ShouldRedirectToUsers() throws Exception {
        UUID targetUserId = UUID.randomUUID();

        mockMvc.perform(post("/admin/users/{userId}/role", targetUserId)
                        .param("role", "ADMIN")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(adminService).updateUserRole(eq(targetUserId), eq(UserRole.ADMIN), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleUserEnabled_ShouldRedirectToUsers() throws Exception {
        UUID targetUserId = UUID.randomUUID();

        mockMvc.perform(post("/admin/users/{userId}/enabled", targetUserId)
                        .param("enabled", "false")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(adminService).toggleUserEnabled(eq(targetUserId), eq(false), eq(adminId), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDashboard_ShouldReturnDashboardView() throws Exception {
        when(adminService.getStats()).thenReturn(AdminStats.builder().totalUsers(5).build());
        when(adminService.getModerationQueue()).thenReturn(emptyModerationQueue());

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attribute("pendingCount", 0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void moderationQueue_ShouldReturnModerationView() throws Exception {
        when(adminService.getModerationQueue()).thenReturn(emptyModerationQueue());

        mockMvc.perform(get("/admin/moderation"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/moderation"))
                .andExpect(model().attributeExists("queue"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveItem_ShouldRedirectToModeration() throws Exception {
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(post("/admin/moderation/{type}/{id}/approve", "product", itemId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/moderation"));

        verify(adminService).approveItem(eq("product"), eq(itemId), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectItem_ShouldRedirectToModeration() throws Exception {
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(post("/admin/moderation/{type}/{id}/reject", "workout", itemId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/moderation"));

        verify(adminService).rejectItem(eq("workout"), eq(itemId), any());
    }
}
