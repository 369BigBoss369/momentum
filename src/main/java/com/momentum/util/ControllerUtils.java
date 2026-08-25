package com.momentum.util;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.nutrition.dto.CreateCompositeFoodDTO;
import com.momentum.nutrition.dto.CreateProductDTO;
import com.momentum.nutrition.dto.CreateRecipeDTO;
import com.momentum.nutrition.dto.EditableFoodData;
import com.momentum.nutrition.dto.enums.FoodItemType;
import com.momentum.nutrition.model.enums.CompositeFoodType;
import com.momentum.nutrition.model.enums.ProductType;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

public class ControllerUtils {
    public static <T extends EditableFoodData> ModelAndView buildFoodEditModelAndView(T dto, UUID foodId, boolean editMode, String editType, String activeTab, ModerationStatus moderationStatus) {
        ModelAndView mv = new ModelAndView("nutrition/create-food");

        mv.addObject("createProductDTO", FoodItemType.valueOf(editType) == FoodItemType.PRODUCT ? dto : new CreateProductDTO());
        mv.addObject("createCompositeFoodDTO", FoodItemType.valueOf(editType) == FoodItemType.COMPOSITE ? dto : new CreateCompositeFoodDTO());
        mv.addObject("createRecipeDTO", FoodItemType.valueOf(editType) == FoodItemType.RECIPE ? dto : new CreateRecipeDTO());

        mv.addObject("productTypes", ProductType.values());
        mv.addObject("compositeFoodTypes", CompositeFoodType.values());
        mv.addObject("editMode", editMode);
        mv.addObject("editType", editType);
        mv.addObject("foodId", foodId);
        mv.addObject("activeTab", activeTab);
        mv.addObject("moderationStatus", moderationStatus);

        return mv;
    }
}

