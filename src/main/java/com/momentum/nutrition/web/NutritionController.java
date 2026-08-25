package com.momentum.nutrition.web;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.nutrition.dto.CreateCompositeFoodDTO;
import com.momentum.nutrition.dto.CreateProductDTO;
import com.momentum.nutrition.dto.CreateRecipeDTO;
import com.momentum.nutrition.model.CompositeFood;
import com.momentum.nutrition.model.Product;
import com.momentum.nutrition.model.Recipe;
import com.momentum.nutrition.model.Step;
import com.momentum.nutrition.model.enums.CompositeFoodType;
import com.momentum.nutrition.model.enums.ProductType;
import com.momentum.nutrition.service.CompositeFoodService;
import com.momentum.nutrition.service.ProductService;
import com.momentum.nutrition.service.RecipeService;
import com.momentum.user.model.User;
import com.momentum.user.service.MealService;
import com.momentum.user.service.UserService;
import com.momentum.util.ControllerUtils;
import com.momentum.util.NutritionMapper;
import com.momentum.user.dto.DailyNutritionView;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/nutrition")
@Slf4j
public class NutritionController {
    private final ProductService productService;
    private final CompositeFoodService compositeFoodService;
    private final RecipeService recipeService;
    private final UserService userService;
    private final MealService mealService;

    @Autowired
    public NutritionController(ProductService productService, CompositeFoodService compositeFoodService, RecipeService recipeService, UserService userService, MealService mealService) {
        this.productService = productService;
        this.compositeFoodService = compositeFoodService;
        this.recipeService = recipeService;
        this.userService = userService;
        this.mealService = mealService;
    }

    @GetMapping("/dashboard")
    public ModelAndView nutritionDashboard(@AuthenticationPrincipal Object principal) {
        ModelAndView mv = new ModelAndView("nutrition/dashboard");
        User currentUser = null;
        DailyNutritionView dailyNutritionView = null;

        if (principal != null) {
            currentUser = userService.getById(userService.extractUserId(principal));
            dailyNutritionView = mealService.buildDailyNutritionView(currentUser);
        }

        if (currentUser == null) {
            dailyNutritionView = mealService.buildEmptyDailyNutritionView();
        }

        mv.addObject("user", currentUser);
        mv.addObject("dailyNutritionView", dailyNutritionView);

        return mv;
    }

    @GetMapping("/food-tracker")
    public ModelAndView foodTracker() {
        ModelAndView mv = new ModelAndView("nutrition/food-tracker");
        mv.addObject("productTypes", ProductType.values());
        mv.addObject("compositeFoodTypes", CompositeFoodType.values());
        return mv;
    }

    @GetMapping("/foods")
    public ModelAndView foods() {
        ModelAndView mv = new ModelAndView("nutrition/recipes");
        mv.addObject("productTypes", ProductType.values());
        mv.addObject("compositeFoodTypes", CompositeFoodType.values());
        return mv;
    }

    @GetMapping("/products/{id}")
    public ModelAndView viewProduct(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        User currentUser = userService.getCurrentUser(principal);
        Product product = productService.getByIdForViewing(id, currentUser);
        UUID userId = currentUser.getId();

        ModelAndView mv = new ModelAndView("nutrition/product-detail");
        mv.addObject("food", product);
        mv.addObject("isOwner", productService.isOwner(product, userId));
        mv.addObject("ownerUsername", productService.getOwnerUsernameIfNotOwner(product, userId));
        mv.addObject("sharedUsersCount", productService.getSharedUsersCount(product));
        mv.addObject("isInLibrary", productService.isInLibrary(product, userId));

        return mv;
    }

    @GetMapping("/composites/{id}")
    public ModelAndView viewCompositeFood(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        UUID userId = userService.extractUserId(principal);
        CompositeFood compositeFood = compositeFoodService.getByIdForViewing(id, userService.getCurrentUser(principal));

        ModelAndView mv = new ModelAndView("nutrition/composite-detail");
        mv.addObject("food", compositeFood);
        mv.addObject("isOwner", compositeFoodService.isOwner(compositeFood, userId));
        mv.addObject("ownerUsername", compositeFoodService.getOwnerUsernameIfNotOwner(compositeFood, userId));
        mv.addObject("sharedUsersCount", compositeFoodService.getSharedUsersCount(compositeFood));
        mv.addObject("isInLibrary", compositeFoodService.isInLibrary(compositeFood, userId));

        return mv;
    }

    @GetMapping("/recipes/{id}")
    public ModelAndView viewRecipe(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        UUID userId = userService.extractUserId(principal);
        Recipe recipe = recipeService.getById(id, userService.getById(userId));

        List<Step> steps = recipe.getSteps().stream()
                .sorted(Comparator.comparing(Step::getStepNumber))
                .toList();

        ModelAndView mv = new ModelAndView("nutrition/recipe-detail");
        mv.addObject("recipe", recipe);
        mv.addObject("steps", steps);
        mv.addObject("currentUserId", userId);
        mv.addObject("isOwner", recipeService.isOwner(recipe, userId));
        mv.addObject("ownerUsername", recipeService.getOwnerUsernameIfNotOwner(recipe, userId));
        mv.addObject("sharedUsersCount", recipeService.getSharedUsersCount(recipe));
        mv.addObject("isInLibrary", recipeService.isInLibrary(recipe, userId));

        return mv;
    }

    @GetMapping("/meal-plan")
    public String mealPlan() {
        return "nutrition/meal-plan";
    }

    @GetMapping("/create-food")
    public ModelAndView createFood() {
        ModelAndView mv = new ModelAndView("nutrition/create-food");

        mv.addObject("createProductDTO", new CreateProductDTO());
        mv.addObject("createCompositeFoodDTO", new CreateCompositeFoodDTO());
        mv.addObject("createRecipeDTO", new CreateRecipeDTO());
        mv.addObject("productTypes", ProductType.values());
        mv.addObject("compositeFoodTypes", CompositeFoodType.values());
        mv.addObject("activeTab", "product");

        return mv;
    }

    @GetMapping("/products/{id}/edit")
    public ModelAndView editProduct(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        Product product = productService.getById(id);

        return ControllerUtils.buildFoodEditModelAndView(
                NutritionMapper.fromProduct(product),
                id,
                true,
                "PRODUCT",
                "product",
                product.getModerationStatus()
        );
    }

    @PutMapping("/products/{id}/edit")
    public ModelAndView updateProduct(@PathVariable UUID id, @AuthenticationPrincipal Object principal, @Valid CreateProductDTO createProductDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Product existingProduct = productService.getById(id);
            return ControllerUtils.buildFoodEditModelAndView(
                    NutritionMapper.fromProduct(existingProduct),
                    id,
                    true,
                    "PRODUCT",
                    "product",
                    existingProduct.getModerationStatus()
            );
        }

        Product product = productService.updateProduct(id, createProductDTO, userService.extractUserId(principal));
        boolean pending = product.getModerationStatus() == ModerationStatus.PENDING;
        return new ModelAndView("redirect:/nutrition/create-food/success?action=updated&pending=" + pending);
    }

    @GetMapping("/composites/{id}/edit")
    public ModelAndView editCompositeFood(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        CompositeFood compositeFood = compositeFoodService.getById(id);

        return ControllerUtils.buildFoodEditModelAndView(
                NutritionMapper.fromCompositeFood(compositeFoodService.getById(id)),
                id,
                true,
                "COMPOSITE",
                "composite",
                compositeFood.getModerationStatus()
        );
    }

    @PutMapping("/composites/{id}/edit")
    public ModelAndView updateCompositeFood(@PathVariable UUID id, @AuthenticationPrincipal Object principal, @Valid CreateCompositeFoodDTO createCompositeFoodDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            CompositeFood existingCompositeFood = compositeFoodService.getById(id);
            return ControllerUtils.buildFoodEditModelAndView(
                    NutritionMapper.fromCompositeFood(existingCompositeFood),
                    id,
                    true,
                    "COMPOSITE",
                    "composite",
                    existingCompositeFood.getModerationStatus()
            );
        }

        CompositeFood compositeFood = compositeFoodService.updateCompositeFood(id, createCompositeFoodDTO, userService.extractUserId(principal));
        boolean pending = compositeFood.getModerationStatus() == ModerationStatus.PENDING;
        return new ModelAndView("redirect:/nutrition/create-food/success?action=updated&pending=" + pending);
    }

    @GetMapping("/recipes/{id}/edit")
    public ModelAndView editRecipe(@PathVariable UUID id, @AuthenticationPrincipal Object principal) {
        User user = userService.getCurrentUser(principal);
        Recipe recipe = recipeService.getById(id, user);

        return ControllerUtils.buildFoodEditModelAndView(
                NutritionMapper.fromRecipe(recipeService.getById(id, user)),
                id,
                true,
                "RECIPE",
                "recipe",
                recipe.getModerationStatus()
        );
    }

    @PutMapping("/recipes/{id}/edit")
    public ModelAndView updateRecipe(@PathVariable UUID id, @AuthenticationPrincipal Object principal, @Valid CreateRecipeDTO createRecipeDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Recipe existingRecipe = recipeService.getById(id, userService.getCurrentUser(principal));
            return ControllerUtils.buildFoodEditModelAndView(
                    NutritionMapper.fromRecipe(existingRecipe),
                    id,
                    true,
                    "RECIPE",
                    "recipe",
                    existingRecipe.getModerationStatus()
            );
        }

        Recipe recipe = recipeService.updateRecipe(id, createRecipeDTO, userService.extractUserId(principal));
        boolean pending = recipe.getModerationStatus() == ModerationStatus.PENDING;
        return new ModelAndView("redirect:/nutrition/create-food/success?action=updated&pending=" + pending);
    }

    @PostMapping("/create-food/product")
    public ModelAndView createProduct(@AuthenticationPrincipal Object principal, @Valid CreateProductDTO createProductDTO, BindingResult bindingResult) {
        UUID userId = userService.extractUserId(principal);

        log.info("Creating product: {} by user: {}", createProductDTO.getName(), userId);

        ModelAndView mv = new ModelAndView("nutrition/create-food");
        mv.addObject("createProductDTO", createProductDTO);
        mv.addObject("createCompositeFoodDTO", new CreateCompositeFoodDTO());
        mv.addObject("createRecipeDTO", new CreateRecipeDTO());
        mv.addObject("productTypes", ProductType.values());
        mv.addObject("compositeFoodTypes", CompositeFoodType.values());


        if (bindingResult.hasErrors()) {
            log.warn("Product creation failed due to validation errors for product: {}", createProductDTO.getName());
            return mv;
        }

        Product product = productService.createProduct(createProductDTO, userId);
        log.info("Product created successfully: {} by user: {}", createProductDTO.getName(), userId);
        boolean pending = product.getModerationStatus() == ModerationStatus.PENDING;
        return new ModelAndView("redirect:/nutrition/create-food/success?action=created&pending=" + pending);
    }

    @PostMapping("/create-food/composite-food")
    public ModelAndView createCompositeFood(@AuthenticationPrincipal Object principal, @Valid CreateCompositeFoodDTO createCompositeFoodDTO, BindingResult bindingResult) {
        ModelAndView mv = new ModelAndView("nutrition/create-food");
        mv.addObject("createProductDTO", new CreateProductDTO());
        mv.addObject("createCompositeFoodDTO", createCompositeFoodDTO);
        mv.addObject("createRecipeDTO", new CreateRecipeDTO());
        mv.addObject("productTypes", ProductType.values());
        mv.addObject("compositeFoodTypes", CompositeFoodType.values());


        if (bindingResult.hasErrors()) {
            return mv;
        }

        CompositeFood compositeFood = compositeFoodService.createCompositeFood(createCompositeFoodDTO, userService.extractUserId(principal));
        boolean pending = compositeFood.getModerationStatus() == ModerationStatus.PENDING;
        return new ModelAndView("redirect:/nutrition/create-food/success?action=created&pending=" + pending);
    }

    @PostMapping("/create-food/recipe")
    public ModelAndView createRecipe(@AuthenticationPrincipal Object principal, @Valid CreateRecipeDTO createRecipeDTO, BindingResult bindingResult) {
        ModelAndView mv = new ModelAndView("nutrition/create-food");
        mv.addObject("createProductDTO", new CreateProductDTO());
        mv.addObject("createCompositeFoodDTO", new CreateCompositeFoodDTO());
        mv.addObject("createRecipeDTO", createRecipeDTO);
        mv.addObject("productTypes", ProductType.values());
        mv.addObject("compositeFoodTypes", CompositeFoodType.values());


        if (bindingResult.hasErrors()) {
            return mv;
        }

        Recipe recipe = recipeService.createRecipe(createRecipeDTO, userService.extractUserId(principal));
        boolean pending = recipe.getModerationStatus() == ModerationStatus.PENDING;
        return new ModelAndView("redirect:/nutrition/create-food/success?action=created&pending=" + pending);
    }

    @GetMapping("/create-food/success")
    public ModelAndView createSuccessfulCreation(@RequestParam(required = false, defaultValue = "created") String action,
                                                 @RequestParam(required = false, defaultValue = "false") boolean pending) {
        ModelAndView mv = new ModelAndView("nutrition/food-created-success");
        mv.addObject("action", action);
        mv.addObject("pending", pending);
        return mv;
    }
}