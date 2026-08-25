package com.momentum.user.web;

import com.momentum.fitness.dto.TrackerDataDTO;
import com.momentum.fitness.model.Workout;
import com.momentum.fitness.service.CompletionService;
import com.momentum.fitness.service.PlanService;
import com.momentum.user.dto.RegisterRequest;
import com.momentum.user.dto.UserBiometricsRequest;
import com.momentum.user.dto.UserGoalsRequest;
import com.momentum.user.dto.UserProfileUpdateRequest;
import com.momentum.user.dto.UserProfileView;
import com.momentum.user.dto.WaterIntakeView;
import com.momentum.user.dto.DailyNutritionView;
import com.momentum.user.model.User;
import com.momentum.user.model.enums.UserRole;
import com.momentum.user.service.UserService;
import com.momentum.user.service.MealService;
import com.momentum.user.service.WaterIntakeService;
import com.momentum.user.service.NutritionActivityService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@Slf4j
public class UserController {
    private final UserService userService;
    private final MealService mealService;
    private final WaterIntakeService waterIntakeService;
    private final NutritionActivityService nutritionActivityService;
    private final CompletionService completionService;
    private final PlanService planService;

    @Autowired
    public UserController(UserService userService,
                         MealService mealService,
                         WaterIntakeService waterIntakeService,
                         NutritionActivityService nutritionActivityService,
                         CompletionService completionService,
                         PlanService planService) {
        this.userService = userService;
        this.mealService = mealService;
        this.waterIntakeService = waterIntakeService;
        this.nutritionActivityService = nutritionActivityService;
        this.completionService = completionService;
        this.planService = planService;
    }

    @GetMapping("/register")
    public ModelAndView register() {
        return new ModelAndView("auth/register", "registerRequest", new RegisterRequest());
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {
        log.info("User registration attempt for username: {}", registerRequest.getUsername());

        if (bindingResult.hasErrors()) {
            log.warn("User registration failed due to validation errors for username: {}", registerRequest.getUsername());
            return new ModelAndView("auth/register");
        }

        userService.register(registerRequest);
        log.info("User registered successfully: {}", registerRequest.getUsername());
        return new ModelAndView("redirect:/dashboard");
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/complete-profile/step1")
    public ModelAndView completeProfileStep1() {
        return new ModelAndView("auth/complete-profile-1", "userBiometricsRequest", new UserBiometricsRequest());
    }

    @PatchMapping("/complete-profile/step1")
    public String completeProfileStep1(@AuthenticationPrincipal Object principal, @Valid UserBiometricsRequest userBiometricsRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "auth/complete-profile-1";
        }

        User user = userService.getCurrentUser(principal);
        userService.updateBiometrics(userBiometricsRequest, user);
        return "redirect:/complete-profile/step2";
    }

    @GetMapping("/complete-profile/step2")
    public ModelAndView completeProfileStep2() {
        return new ModelAndView("auth/complete-profile-2", "userGoalsRequest", new UserGoalsRequest());
    }

    @PatchMapping("/complete-profile/step2")
    public ModelAndView completeProfileStep2(@AuthenticationPrincipal Object principal, @Valid UserGoalsRequest userGoalsRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("auth/complete-profile-2");
        }

        User user = userService.getCurrentUser(principal);
        userService.completeRegistration(userGoalsRequest, user);
        return new ModelAndView("redirect:/dashboard");
    }

    @GetMapping("/dashboard")
    public ModelAndView dashboard(@AuthenticationPrincipal Object principal) {
        UUID userId = userService.extractUserId(principal);
        User user = userService.getById(userId);


        if (user.getRole() == UserRole.ADMIN) {
            return new ModelAndView("redirect:/admin/dashboard");
        }

        ModelAndView mv = new ModelAndView("dashboard");


        mv.addObject("userName", user.getUsername());


        long workoutsThisWeek = completionService.getWorkoutsCompletedThisWeek(userId);
        long caloriesToday = completionService.getCaloriesBurnedToday(userId);

        mv.addObject("workoutsThisWeek", workoutsThisWeek);
        mv.addObject("caloriesToday", caloriesToday);


        WaterIntakeView waterIntake = waterIntakeService.getWaterForDate(userId, java.time.LocalDate.now());
        double waterAmount = waterIntake != null && waterIntake.getTotalAmount() != null ? waterIntake.getTotalAmount() : 0.0;

        mv.addObject("waterMl", Math.round(waterAmount));


        DailyNutritionView nutritionView = mealService.buildDailyNutritionView(user);
        mv.addObject("caloriesConsumed", nutritionView.getCaloriesConsumed());
        mv.addObject("caloriesGoal", nutritionView.getCaloriesGoal());
        mv.addObject("proteinConsumed", nutritionView.getProteinConsumed());
        mv.addObject("carbsConsumed", nutritionView.getCarbsConsumed());
        mv.addObject("fatConsumed", nutritionView.getFatConsumed());


        double proteinProgress = nutritionView.getProteinGoal() > 0 ? (double) nutritionView.getProteinConsumed() / nutritionView.getProteinGoal() * 100 : 0;
        double carbsProgress = nutritionView.getCarbsGoal() > 0 ? (double) nutritionView.getCarbsConsumed() / nutritionView.getCarbsGoal() * 100 : 0;
        double fatProgress = nutritionView.getFatGoal() > 0 ? (double) nutritionView.getFatConsumed() / nutritionView.getFatGoal() * 100 : 0;

        mv.addObject("proteinProgress", Math.min(proteinProgress, 100));
        mv.addObject("carbsProgress", Math.min(carbsProgress, 100));
        mv.addObject("fatProgress", Math.min(fatProgress, 100));


        TrackerDataDTO trackerData = planService.getTrackerDataForUser(userId);
        List<Map<String, Object>> todaysWorkouts = new ArrayList<>();

        if (trackerData.getCurrentPlanDay() != null && trackerData.getCurrentPlanDay().getWorkouts() != null &&
                !trackerData.getCurrentPlanDay().getWorkouts().isEmpty()) {

            List<Workout> workouts = trackerData.getCurrentPlanDay().getWorkouts();

            for (int i = 0; i < workouts.size(); i++) {
                Workout workout = workouts.get(i);
                boolean isCompleted = completionService.isWorkoutCompleted(
                    userId,
                    workout.getId(),
                    trackerData.getCurrentPlanDay().getId(),
                    i
                );

                Map<String, Object> workoutData = new HashMap<>();
                workoutData.put("workout", workout);
                workoutData.put("completed", isCompleted);
                workoutData.put("position", i);

                todaysWorkouts.add(workoutData);
            }
        }

        mv.addObject("todaysWorkouts", todaysWorkouts);

        return mv;
    }

    @GetMapping("/profile")
    public ModelAndView profile(@AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);

        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView mv = new ModelAndView("profile", "user", UserProfileView.from(user));
        mv.addObject("totalWorkoutsCompleted", completionService.getTotalWorkoutsCompleted(user.getId()));
        mv.addObject("totalCaloriesBurned", completionService.getTotalCaloriesBurned(user.getId()));
        mv.addObject("totalHoursExercised", completionService.getTotalHoursExercised(user.getId()));
        mv.addObject("dayStreak", completionService.getCurrentDayStreak(user.getId()));
        return mv;
    }

    @PatchMapping("/profile/update/biometrics")
    public String updateProfile(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute UserProfileUpdateRequest profileRequest, BindingResult bindingResult, Model model) {
        User user = userService.getCurrentUser(principal);

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", UserProfileView.from(user));
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "userProfileUpdateRequest", bindingResult);
            return "profile";
        }

        userService.updateProfile(profileRequest, user);
        return "redirect:/profile";
    }

    @PatchMapping("/profile/update/goals")
    public String updateGoals(@AuthenticationPrincipal Object principal, @Valid @ModelAttribute UserGoalsRequest userGoalsRequest, BindingResult bindingResult, Model model) {
        User user = userService.getCurrentUser(principal);

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", UserProfileView.from(user));
            return "profile";
        }

        userService.updateGoals(userGoalsRequest, user);
        return "redirect:/profile";
    }

}

