package com.momentum.fitness.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ActivitySearchDTO {
    private UUID id;
    private String name;
    private String type;
    private String typeDisplayName;

    @Builder.Default
    private List<WorkoutExerciseDTO> exercises = new ArrayList<>();
    @Builder.Default
    private List<String> muscleGroups = new ArrayList<>();

    private String imageUrl;
    private Integer exerciseCount;

    private String owner;
    private Boolean isPublic;
    private Boolean inLibrary;
    private Integer sharedUsers;
}

