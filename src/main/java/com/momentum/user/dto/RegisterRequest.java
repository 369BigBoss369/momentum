package com.momentum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 32, message = "The username must be between 3 and 32 characters long")
    private String username;
    @NotBlank
    @Size(min = 8, message = "The password must be at least 8 characters long")
    private String password;
    @Email(message = "Invalid email format")
    private String email;
}

