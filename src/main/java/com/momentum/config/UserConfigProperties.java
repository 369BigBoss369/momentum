package com.momentum.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@NoArgsConstructor
@Getter
@Setter

@Configuration
@ConfigurationProperties(prefix = "users")
public class UserConfigProperties {
    private DefaultUser defaultUser;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class DefaultUser {
        private String username;
        private String password;
        private String email;
    }
}

