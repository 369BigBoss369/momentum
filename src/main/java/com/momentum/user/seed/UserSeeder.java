package com.momentum.user.seed;

import com.momentum.user.model.User;
import com.momentum.user.model.enums.AuthProvider;
import com.momentum.user.model.enums.UserRole;
import com.momentum.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        createAdminUser();
    }

    private void createAdminUser() {
        Optional<User> existingAdmin = userRepository.findByUsername("admin");

        if (existingAdmin.isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@momentum.com")
                    .role(UserRole.ADMIN)
                    .provider(AuthProvider.LOCAL)
                    .build();

            userRepository.save(admin);
            
        }
    }
}





