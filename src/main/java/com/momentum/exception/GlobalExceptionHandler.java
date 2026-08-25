package com.momentum.exception;

import com.momentum.exception.fitness.ActivityNotFoundException;
import com.momentum.exception.fitness.CustomActivityAlreadyExists;
import com.momentum.exception.nutrition.CustomFoodAlreadyExists;
import com.momentum.exception.nutrition.EmptyRecipeException;
import com.momentum.exception.nutrition.FoodNotFoundException;
import com.momentum.exception.user.InvalidCredentialsException;
import com.momentum.exception.user.InvalidUserGoalException;
import com.momentum.exception.user.UserAlreadyExistsException;
import com.momentum.exception.user.UserNotFoundException;
import com.momentum.fitness.dto.CreateExerciseDTO;
import com.momentum.fitness.dto.CreatePlanDTO;
import com.momentum.fitness.dto.CreateWorkoutDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleUserNotFoundException(UserNotFoundException e) {
        logger.warn("User not found: {}", e.getMessage());

        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("error", "User Not Found");
        modelAndView.addObject("message", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ModelAndView handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        logger.warn("User already exists: {}", e.getMessage());

        ModelAndView modelAndView = new ModelAndView("auth/register");
        modelAndView.addObject("error", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ModelAndView handleInvalidCredentialsException(InvalidCredentialsException e) {
        logger.warn("Invalid credentials attempt: {}", e.getMessage());

        ModelAndView modelAndView = new ModelAndView("auth/login");
        modelAndView.addObject("error", "Invalid email or password");

        return modelAndView;
    }

    @ExceptionHandler(InvalidUserGoalException.class)
    public ModelAndView handleInvalidUserGoalException(InvalidUserGoalException e) {
        logger.warn("Invalid user goal: {}", e.getMessage());

        ModelAndView modelAndView = new ModelAndView("auth/complete-profile-2");
        modelAndView.addObject("error", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(ActivityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleActivityNotFoundException(ActivityNotFoundException e) {
        logger.warn("Activity not found: {}", e.getMessage());

        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("error", "Activity Not Found");
        modelAndView.addObject("message", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(CustomActivityAlreadyExists.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView handleCustomActivityAlreadyExists(CustomActivityAlreadyExists e) {
        logger.warn("Custom activity already exists: {}", e.getMessage());

        ModelAndView modelAndView = new ModelAndView("fitness/create-workout");
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("createExerciseDTO", new CreateExerciseDTO());
        modelAndView.addObject("createWorkoutDTO", new CreateWorkoutDTO());
        modelAndView.addObject("createPlanDTO", new CreatePlanDTO());
        modelAndView.addObject("editMode", false);
        modelAndView.addObject("editType", null);
        modelAndView.addObject("editId", null);
        modelAndView.addObject("activeTab", "exercise");

        return modelAndView;
    }

    @ExceptionHandler(FoodNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleFoodNotFoundException(FoodNotFoundException e) {
        logger.warn("Food not found: {}", e.getMessage());

        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("error", "Food Not Found");
        modelAndView.addObject("message", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(CustomFoodAlreadyExists.class)
    public ModelAndView handleCustomFoodAlreadyExists(CustomFoodAlreadyExists e) {
        logger.warn("Custom food already exists: {}", e.getMessage());

        ModelAndView modelAndView = new ModelAndView("nutrition/create-food");
        modelAndView.addObject("error", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(EmptyRecipeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleEmptyRecipeException(EmptyRecipeException e) {
        logger.warn("Empty recipe: {}", e.getMessage());

        ModelAndView modelAndView = new ModelAndView("nutrition/recipes");
        modelAndView.addObject("error", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(UnauthorizedResourceAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleUnauthorizedResourceAccessException(UnauthorizedResourceAccessException e) {
        logger.warn("Unauthorized resource access: {}", e.getMessage());

        ModelAndView modelAndView = new ModelAndView("error/403");
        modelAndView.addObject("error", "Access Denied");
        modelAndView.addObject("message", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleValidationExceptions(MethodArgumentNotValidException e) {
        logger.warn("Validation error: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("validationErrors", errors);
        modelAndView.addObject("org.springframework.validation.BindingResult." + e.getBindingResult().getObjectName(), e.getBindingResult());

        String viewName = "error/validation-error";
        modelAndView.setViewName(viewName);

        return modelAndView;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException e) {
        logger.error("Illegal argument: {}", e.getMessage(), e);

        ModelAndView modelAndView = new ModelAndView("error/400");
        modelAndView.addObject("error", "Bad Request");
        modelAndView.addObject("message", "Invalid request parameters");

        return modelAndView;
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView handleIllegalStateException(IllegalStateException e) {
        logger.error("Illegal state: {}", e.getMessage(), e);

        ModelAndView modelAndView = new ModelAndView("error/409");
        modelAndView.addObject("error", "Conflict");
        modelAndView.addObject("message", e.getMessage());

        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(Exception e) {
        logger.error("Unexpected error occurred: {}", e.getMessage(), e);

        ModelAndView modelAndView = new ModelAndView("error/500");
        modelAndView.addObject("error", "Internal Server Error");
        modelAndView.addObject("message", "An unexpected error occurred. Please try again later.");

        return modelAndView;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        logger.error("Runtime exception: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
