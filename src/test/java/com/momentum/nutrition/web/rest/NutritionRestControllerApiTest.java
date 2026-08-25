package com.momentum.nutrition.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.config.TestConfig;
import com.momentum.nutrition.dto.FoodSearchDTO;
import com.momentum.nutrition.service.*;
import com.momentum.user.dto.MealFoodDTO;
import com.momentum.user.dto.WaterIntakeRequest;
import com.momentum.user.dto.WaterIntakeView;
import com.momentum.user.model.User;
import com.momentum.user.service.MealService;
import com.momentum.user.service.NutritionActivityService;
import com.momentum.user.service.UserService;
import com.momentum.user.service.WaterIntakeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NutritionRestController.class)
@Import(TestConfig.class)
class NutritionRestControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FoodService foodService;

    @MockBean
    private MealService mealService;

    @MockBean
    private WaterIntakeService waterIntakeService;

    @MockBean
    private NutritionActivityService nutritionActivityService;

    @MockBean
    private FoodSearchService foodSearchService;

    @MockBean
    private ProductService productService;

    @MockBean
    private CompositeFoodService compositeFoodService;

    @MockBean
    private RecipeService recipeService;

    @MockBean
    private UserService userService;

    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder().id(userId).username("testuser").build();
        when(userService.getCurrentUser(any())).thenReturn(mockUser);
    }

    @Test
    @WithMockUser
    void searchFoods_ShouldReturnFoodList() throws Exception {
        FoodSearchDTO dto = FoodSearchDTO.builder().id(UUID.randomUUID()).name("Chicken Breast").build();

        when(foodService.searchByName(anyString(), anyInt(), any())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/v1/nutrition/foods").param("query", "chicken").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void getTodayMeals_ShouldReturnMealList() throws Exception {
        when(mealService.getMealsForDate(any(), any())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/v1/nutrition/meals"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void addFoodToMeal_ShouldReturnCreated() throws Exception {
        MealFoodDTO dto = MealFoodDTO.builder().foodId(UUID.randomUUID()).amount(150).build();

        mockMvc.perform(post("/api/v1/nutrition/meals/food")
                        .param("mealType", "BREAKFAST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(mealService).addFoodToMeal(org.mockito.ArgumentMatchers.eq("BREAKFAST"), any(), any(), any());
    }

    @Test
    @WithMockUser
    void addFoodToMeal_ShouldReturnBadRequest_WhenAmountMissing() throws Exception {
        MealFoodDTO dto = MealFoodDTO.builder().foodId(UUID.randomUUID()).build();

        mockMvc.perform(post("/api/v1/nutrition/meals/food")
                        .param("mealType", "BREAKFAST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void removeFoodFromMeal_ShouldReturnNoContent() throws Exception {
        UUID mealFoodId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/nutrition/meals/food/{mealFoodId}", mealFoodId))
                .andExpect(status().isNoContent());

        verify(mealService).removeMealFood(mealFoodId, userId);
    }

    @Test
    @WithMockUser
    void getWater_ShouldReturnWaterIntake() throws Exception {
        when(waterIntakeService.getWaterForDate(any(), any())).thenReturn(new WaterIntakeView());

        mockMvc.perform(get("/api/v1/nutrition/water"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void logWater_ShouldReturnUpdatedWaterIntake() throws Exception {
        WaterIntakeRequest request = new WaterIntakeRequest();
        request.setAmount(250.0);
        when(waterIntakeService.getWaterForDate(any(), any())).thenReturn(new WaterIntakeView());

        mockMvc.perform(post("/api/v1/nutrition/water")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(waterIntakeService).logWater(userId, 250.0);
    }

    @Test
    @WithMockUser
    void addProductToLibrary_ShouldReturnOk() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/nutrition/products/{productId}/library", productId))
                .andExpect(status().isOk());

        verify(productService).addToLibrary(productId, userId);
    }

    @Test
    @WithMockUser
    void removeProductFromLibrary_ShouldReturnOk() throws Exception {
        UUID productId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/nutrition/products/{productId}/library", productId))
                .andExpect(status().isOk());

        verify(productService).removeFromLibrary(productId, userId);
    }

    @Test
    @WithMockUser
    void addRecipeToLibrary_ShouldReturnOk() throws Exception {
        UUID recipeId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/nutrition/recipes/{recipeId}/library", recipeId))
                .andExpect(status().isOk());

        verify(recipeService).addToLibrary(recipeId, userId);
    }

    @Test
    @WithMockUser
    void removeCompositeFoodFromLibrary_ShouldReturnOk() throws Exception {
        UUID compositeId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/nutrition/composites/{compositeId}/library", compositeId))
                .andExpect(status().isOk());

        verify(compositeFoodService).removeFromLibrary(compositeId, userId);
    }
}
