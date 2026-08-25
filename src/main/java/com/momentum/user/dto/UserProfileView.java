package com.momentum.user.dto;

import com.momentum.user.model.User;
import com.momentum.user.model.enums.GenderType;
import com.momentum.user.model.enums.UserGoal;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter

public class UserProfileView {
    private String username;
    private String email;

    private LocalDateTime createdAt;

    private Integer height;
    private Double weight;
    private Integer age;
    private GenderType gender;

    private UserGoal goal;
    private Double targetWeight;
    private Double pace;

    public static UserProfileView from(User user) {
        return UserProfileView.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .height(user.getHeight())
                .weight(user.getWeight())
                .age(user.getAge())
                .gender(user.getGender())
                .goal(user.getGoal())
                .targetWeight(user.getTargetWeight())
                .pace(user.getPace())
                .build();
    }
}

