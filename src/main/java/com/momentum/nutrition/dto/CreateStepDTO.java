package com.momentum.nutrition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateStepDTO {
    @NotBlank
    @Size(max = 100)
    private String title;
    @NotBlank
    @Size(max = 1000)
    private String description;
    @URL
    @Size(max = 2048, message = "Image URL must be at most 2048 characters")
    private String imageUrl;
    @NotNull
    private Integer stepNumber;
}

