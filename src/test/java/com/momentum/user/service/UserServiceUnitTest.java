package com.momentum.user.service;

import com.momentum.user.dto.RegisterRequest;
import com.momentum.user.model.User;
import com.momentum.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("testuser", "password123", "test@example.com");
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();
    }

    @Test
    void register_ShouldCreateUser_WhenValidRequest() {
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        userService.register(registerRequest);

        verify(passwordEncoder).encode(registerRequest.getPassword());
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        User result = userService.getById(testUser.getId());

        assertEquals(testUser, result);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        var result = userService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.loadUserByUsername("testuser"));
    }
}