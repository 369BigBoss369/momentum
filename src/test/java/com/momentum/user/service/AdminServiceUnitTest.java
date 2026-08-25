package com.momentum.user.service;

import com.momentum.user.model.User;
import com.momentum.user.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminService adminService;

    @Mock
    private RedirectAttributes redirectAttributes;

    private UUID userId;
    private User testUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .build();
    }

    @Test
    void updateUserRole_ShouldUpdateRoleSuccessfully() {
        adminService.updateUserRole(userId, UserRole.ADMIN, redirectAttributes);

        verify(userService).updateUserRole(userId, UserRole.ADMIN);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
    }

    @Test
    void updateUserRole_ShouldHandleException() {
        doThrow(new RuntimeException("Update failed")).when(userService).updateUserRole(userId, UserRole.ADMIN);

        adminService.updateUserRole(userId, UserRole.ADMIN, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
    }
}
