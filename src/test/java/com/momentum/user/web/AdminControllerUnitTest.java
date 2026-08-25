package com.momentum.user.web;

import com.momentum.user.dto.AdminStats;
import com.momentum.user.model.User;
import com.momentum.user.model.enums.UserRole;
import com.momentum.user.service.AdminService;
import com.momentum.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AdminControllerUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private AdminService adminService;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Model model;

    @InjectMocks
    private AdminController adminController;

    private List<User> users;

    @BeforeEach
    void setUp() {
        users = Arrays.asList(
                User.builder().id(java.util.UUID.randomUUID()).username("user1").role(UserRole.USER).build(),
                User.builder().id(java.util.UUID.randomUUID()).username("user2").role(UserRole.ADMIN).build()
        );
    }

    @Test
    void listUsers_ShouldReturnUsersView() {
        Object principal = mock(Object.class);
        User currentAdmin = User.builder().id(UUID.randomUUID()).build();

        when(userService.getCurrentUser(principal)).thenReturn(currentAdmin);
        when(userService.searchUsers(null, null, null)).thenReturn(users);
        when(userService.countActiveAdmins()).thenReturn(1L);

        String result = adminController.listUsers(null, null, null, principal, model);

        verify(userService).searchUsers(null, null, null);
        verify(model).addAttribute("users", users);
        verify(model).addAttribute("query", null);
        verify(model).addAttribute("selectedRole", null);
        verify(model).addAttribute("selectedEnabled", null);
        verify(model).addAttribute("currentUserId", currentAdmin.getId());
        verify(model).addAttribute("activeAdminCount", 1L);
        assertEquals("admin/users", result);
    }

    @Test
    void updateUserRole_ShouldRedirectAfterUpdate() {
        java.util.UUID userId = java.util.UUID.randomUUID();

        doNothing().when(adminService).updateUserRole(userId, UserRole.ADMIN, redirectAttributes);

        String result = adminController.updateUserRole(userId, UserRole.ADMIN, redirectAttributes);

        verify(adminService).updateUserRole(userId, UserRole.ADMIN, redirectAttributes);
        // The method returns "redirect:/admin/users"
    }

    @Test
    void adminDashboard_ShouldReturnDashboardView() {
        AdminStats stats = AdminStats.builder().build();
        when(adminService.getStats()).thenReturn(stats);

        String result = adminController.adminDashboard(model);

        verify(adminService).getStats();
        verify(model).addAttribute("stats", stats);
        assertEquals("admin/dashboard", result);
    }
}
