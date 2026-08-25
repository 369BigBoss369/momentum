package com.momentum.user.service;

import com.momentum.config.UserConfigProperties;
import com.momentum.user.dto.RegisterRequest;
import com.momentum.user.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultUserInitializer implements ApplicationRunner {
    private final UserService userService;
    private final UserConfigProperties userConfigProperties;

    @Autowired
    public DefaultUserInitializer(UserService userService, UserConfigProperties userConfigProperties) {
        this.userService = userService;
        this.userConfigProperties = userConfigProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userService.getByUsername(userConfigProperties.getDefaultUser().getUsername()).isPresent()) {
            return;
        }

        userService.register(
                RegisterRequest.builder()
                        .username(userConfigProperties.getDefaultUser().getUsername())
                        .password(userConfigProperties.getDefaultUser().getPassword())
                        .email(userConfigProperties.getDefaultUser().getEmail())
                        .build(),
                UserRole.ADMIN
        );
    }
}

