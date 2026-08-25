package com.momentum.user.dto;

import com.momentum.user.model.enums.GenderType;
import jakarta.validation.constraints.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class UserProfileUpdateRequest {
    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Height is required.")
    @Min(value = 50, message = "Height must be at least 50 cm")
    @Max(value = 300, message = "Height cannot exceed 300 cm")
    private Integer height;

    @NotNull(message = "Weight is required.")
    @DecimalMin(value = "20.0", message = "Weight must be at least 20 kg")
    @DecimalMax(value = "400.0", message = "Weight cannot exceed 400 kg")
    private Double weight;

    @NotNull(message = "Age is required.")
    @Min(value = 10, message = "Age must be at least 10")
    @Max(value = 120, message = "Age cannot exceed 120")
    private Integer age;

    @NotNull(message = "Gender is required.")
    private GenderType gender;
}

