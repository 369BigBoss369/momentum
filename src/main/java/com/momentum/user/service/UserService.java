package com.momentum.user.service;

import com.momentum.exception.user.InvalidUserGoalException;
import com.momentum.exception.user.UserAlreadyExistsException;
import com.momentum.exception.user.UserNotFoundException;
import com.momentum.security.AuthenticationMetadata;
import com.momentum.security.CustomOidcUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import com.momentum.user.dto.RegisterRequest;
import com.momentum.user.dto.UserBiometricsRequest;
import com.momentum.user.dto.UserGoalsRequest;
import com.momentum.user.dto.UserProfileUpdateRequest;
import com.momentum.user.model.User;
import com.momentum.user.model.enums.AuthProvider;
import com.momentum.user.model.enums.GenderType;
import com.momentum.user.model.enums.UserGoal;
import com.momentum.user.model.enums.UserRole;
import com.momentum.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService self;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, @Lazy UserService self) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.self = self;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));

        if (user.getPassword() == null) {
            throw new UsernameNotFoundException("User has no password - please use OAuth2 login");
        }

        return new AuthenticationMetadata(user.getId(), user.getUsername(), user.getPassword(), user.getRole(), user.getEnabled());
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {
        register(registerRequest, UserRole.USER);
    }

    @Transactional
    public void register(RegisterRequest registerRequest, UserRole role) {
        Optional<User> optional = userRepository.findByUsername(registerRequest.getUsername());

        if (optional.isPresent()) {
            throw new UserAlreadyExistsException("User with username " + registerRequest.getUsername() + " already exists");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .build();

        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isEmpty()) {
            user.setEmail(registerRequest.getEmail());
        }

        userRepository.save(user);
    }

    @Transactional
    public void updateBiometrics(UserBiometricsRequest biometricsRequest, User user) {
        user.setHeight(biometricsRequest.getHeight());
        user.setWeight(biometricsRequest.getWeight());
        user.setAge(biometricsRequest.getAge());
        user.setGender(biometricsRequest.getGender());

        userRepository.save(user);
    }

    @CacheEvict(value = "userProfiles", allEntries = true)
    public void updateProfile(UserProfileUpdateRequest profileRequest, User user) {
        user.setUsername(profileRequest.getUsername());
        user.setEmail(profileRequest.getEmail());
        user.setHeight(profileRequest.getHeight());
        user.setWeight(profileRequest.getWeight());
        user.setAge(profileRequest.getAge());
        user.setGender(profileRequest.getGender());

        calculateNutritionalRequirements(user);

        userRepository.save(user);
    }

    @Transactional
    public void completeRegistration(UserGoalsRequest userGoalsRequest, User user) {
        updateGoals(userGoalsRequest, user);
        calculateNutritionalRequirements(user);
    }

    @Transactional
    public void updateGoals(UserGoalsRequest userGoalsRequest, User user) {
        if (userGoalsRequest.getTargetWeight() >= user.getWeight()) {
            throw new InvalidUserGoalException("Target weight must be lower than current weight");
        }

        user.setGoal(userGoalsRequest.getGoal());

        if (userGoalsRequest.getGoal() == UserGoal.MAINTAIN_WEIGHT || userGoalsRequest.getGoal() == UserGoal.RECOMPOSITION) {
            user.setTargetWeight(null);
            user.setPace(null);
        } else {
            user.setTargetWeight(userGoalsRequest.getTargetWeight());
            user.setPace(userGoalsRequest.getPace());
        }

        calculateNutritionalRequirements(user);

        userRepository.save(user);
    }

    @Cacheable(value = "userProfiles", key = "'id_' + #id")
    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id.toString()));
    }

    public User getByIdWithCurrentPlan(UUID id) {
        Optional<User> userOpt = userRepository.findByIdWithCurrentPlan(id);
        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
            throw new UserNotFoundException(id.toString());
        }
    }

    @Cacheable(value = "userProfiles", key = "'username_' + #username")
    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User createOrGetOAuth2User(AuthProvider provider, String providerId, String email, String name) {
        log.debug("Creating or getting OAuth2 user - provider: {}, providerId: {}, email: {}, name: {}",
                 provider, providerId, email, name);

        User user = getByProviderAndProviderId(provider, providerId);
        if (user != null) {
            log.debug("Found existing OAuth2 user: {}", user.getUsername());
            return user;
        }

        if (email != null && !email.trim().isEmpty()) {
            Optional<User> existingUserByEmail = userRepository.findByEmail(email);
            if (existingUserByEmail.isPresent()) {
                User existingUser = existingUserByEmail.get();

                log.info("Found existing user with email {}, linking OAuth2 provider {} (providerId: {})",
                        email, provider, providerId);


                existingUser.setProviderId(providerId);

                save(existingUser);
                log.info("Successfully linked OAuth2 provider to existing user: {}", existingUser.getUsername());
                return existingUser;
            }
        }

        log.info("No existing account found, creating new OAuth2 user");
        String username = generateUsername(name, email);

        user = User.builder()
                .username(username)
                .email(email)
                .password(null)
                .provider(provider)
                .providerId(providerId)
                .role(UserRole.USER)
                .build();

        save(user);
        log.info("Successfully created new OAuth2 user: {} with provider: {}", user.getUsername(), provider);
        return user;
    }

    public User getByProviderAndProviderId(AuthProvider provider, String providerId) {
        log.debug("Looking up user by provider: {} and providerId: {}", provider, providerId);
        try {

            User user = userRepository.findByProviderAndProviderId(provider, providerId);
            if (user != null) {
                log.debug("Found user: {} by exact provider match", user.getUsername());
                return user;
            }

            log.debug("No exact match found, checking for linked accounts with providerId: {}", providerId);
            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                if (providerId.equals(u.getProviderId())) {
                    log.debug("Found linked user: {} with providerId match", u.getUsername());
                    return u;
                }
            }

            log.debug("User not found in database for provider: {}, providerId: {}", provider, providerId);
            return null;
        } catch (Exception e) {
            log.error("Error looking up user by provider and providerId: ", e);
            return null;
        }
    }

    private String generateUsername(String name, String email) {
        if (name != null && !name.trim().isEmpty()) {
            return name.replaceAll("\\s+", "").toLowerCase();
        }
        if (email != null && !email.isEmpty()) {
            return email.split("@")[0];
        }
        return "oauth2user";
    }

    private void calculateNutritionalRequirements(User user) {
        double bmr;
        if (user.getGender() == GenderType.MALE) {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        } else {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        }

        int maxCalories = getMaxCalories(user, bmr);
        int carbCalories = (int) (maxCalories * 0.5);
        int proteinCalories = (int) (maxCalories * 0.3);
        int fatCalories = (int) (maxCalories * 0.2);

        user.setMaxCalories(maxCalories);
        user.setMaxCarbohydrates(carbCalories / 4);
        user.setMaxProtein(proteinCalories / 4);
        user.setMaxFat(fatCalories / 9);

        userRepository.save(user);
    }

    private static int getMaxCalories(User user, double bmr) {
        double activityFactor = 1.2;
        double tdee = bmr * activityFactor;

        int maxCalories;
        UserGoal goal = user.getGoal();
        switch (goal) {
            case LOSE_WEIGHT -> {
                double weightDifference = user.getWeight() - user.getTargetWeight();

                if (weightDifference > 0 && user.getPace() != null && user.getPace() > 0) {
                    double weeksToGoal = weightDifference / user.getPace();
                    double weeklyCalorieDeficit = (weightDifference * 7700) / weeksToGoal;
                    double dailyCalorieDeficit = weeklyCalorieDeficit / 7;

                    maxCalories = (int) (tdee - dailyCalorieDeficit);
                } else {
                    maxCalories = (int) (tdee - 500);
                }
            }
            case GAIN_MUSCLE -> {
                double weightDifference = user.getTargetWeight() - user.getWeight();

                if (weightDifference > 0 && user.getPace() != null && user.getPace() > 0) {
                    double weeksToGoal = weightDifference / user.getPace();
                    double weeklyCalorieSurplus = (weightDifference * 3000) / weeksToGoal;
                    double dailyCalorieSurplus = weeklyCalorieSurplus / 7;

                    maxCalories = (int) (tdee + dailyCalorieSurplus);
                } else {
                    maxCalories = (int) (tdee + 300);
                }
            }
            case MAINTAIN_WEIGHT -> maxCalories = (int) tdee;
            case RECOMPOSITION -> maxCalories = (int) (tdee - 200);
            default -> maxCalories = (int) tdee;
        }
        return maxCalories;
    }

    @Transactional
    public void save(User user) {
        log.debug("Saving user: {} with provider: {} and providerId: {}", user.getUsername(), user.getProvider(), user.getProviderId());
        User savedUser = userRepository.save(user);
        log.debug("User saved with ID: {}", savedUser.getId());

        userRepository.flush();
        log.debug("User flushed to database");
    }

    public User getCurrentUser(Object principal) {
        log.debug("Getting current user for principal type: {}", principal.getClass().getSimpleName());

        if (principal instanceof CustomOidcUser) {
            User user = ((CustomOidcUser) principal).getUser();
            log.debug("Found CustomOidcUser with user: {}", user != null ? user.getUsername() : "null");
            return user;
        } else if (principal instanceof AuthenticationMetadata) {
            User user = self.getById(((AuthenticationMetadata) principal).getId());
            log.debug("Found AuthenticationMetadata user: {}", user != null ? user.getUsername() : "null");
            return user;
        } else if (principal instanceof DefaultOidcUser oidcUser) {
            String providerId = oidcUser.getSubject();

            log.debug("Looking up Google OAuth2 user with providerId: {}", providerId);
            User user = getByProviderAndProviderId(AuthProvider.GOOGLE, providerId);

            if (user == null) {
                log.warn("Google OAuth2 user not found - this should not happen if success handler worked");
                String email = oidcUser.getEmail();
                String name = oidcUser.getFullName();
                log.info("Creating Google OAuth2 user as fallback in getCurrentUser");
                return self.createOrGetOAuth2User(AuthProvider.GOOGLE, providerId, email, name);
            }

            return user;
        } else {
            log.error("Unknown authentication principal type: {}", principal.getClass());
            throw new IllegalStateException("Unknown authentication principal type: " + principal.getClass());
        }
    }

    public LocalDate getCurrentPlanStartDate(UUID userId) {
        User user = self.getById(userId);
        return user.getCurrentPlanStartDate();
    }

    @Transactional
    public void clearCurrentPlan(UUID userId) {
        User user = self.getById(userId);
        user.setCurrentPlan(null);
        user.setCurrentPlanStartDate(null);
        userRepository.save(user);
    }

    public UUID extractUserId(Object principal) {
        if (principal instanceof AuthenticationMetadata) {
            return ((AuthenticationMetadata) principal).getId();
        } else if (principal instanceof com.momentum.security.CustomOidcUser customOidcUser) {
            return customOidcUser.getUser().getId();
        } else if (principal instanceof DefaultOidcUser oidcUser) {
            String providerId = oidcUser.getSubject();

            try {
                User user = getByProviderAndProviderId(AuthProvider.GOOGLE, providerId);
                return user != null ? user.getId() : null;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void updateUserRole(UUID userId, UserRole newRole) {
        User user = self.getById(userId);
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Transactional
    public void setEnabled(UUID userId, boolean enabled) {
        User user = self.getById(userId);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    public List<User> searchUsers(String query, UserRole role, Boolean enabled) {
        String normalizedQuery = (query == null || query.isBlank()) ? null : query;
        return userRepository.searchAndFilterUsers(normalizedQuery, role, enabled);
    }

    public long countActiveAdmins() {
        return userRepository.findByRole(UserRole.ADMIN).stream()
                .filter(u -> u.getEnabled() == null || u.getEnabled())
                .count();
    }

    public long countByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    public Map<UUID, String> getUsernamesByIds(Set<UUID> ids) {
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }
}