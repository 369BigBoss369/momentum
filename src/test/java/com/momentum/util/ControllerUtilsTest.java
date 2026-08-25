package com.momentum.util;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.nutrition.dto.CreateProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

class ControllerUtilsTest {

    @Test
    void buildFoodEditModelAndView_ShouldReturnModelAndView() {
        CreateProductDTO dto = new CreateProductDTO();
        UUID foodId = UUID.randomUUID();

        ModelAndView mv = ControllerUtils.buildFoodEditModelAndView(dto, foodId, true, "PRODUCT", "product", ModerationStatus.APPROVED);

        assertNotNull(mv);
        assertEquals("nutrition/create-food", mv.getViewName());
        assertTrue((Boolean) mv.getModel().get("editMode"));
        assertEquals("PRODUCT", mv.getModel().get("editType"));
        assertEquals(ModerationStatus.APPROVED, mv.getModel().get("moderationStatus"));
    }

    @Test
    void uuidFromString_ShouldWork() {
        String testId = "550e8400-e29b-41d4-a716-446655440000";
        assertDoesNotThrow(() -> UUID.fromString(testId));
    }
}
