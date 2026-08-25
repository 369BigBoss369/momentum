package com.momentum.core.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndexControllerTest {

    private final IndexController indexController = new IndexController();

    @Test
    void index_ShouldReturnIndexView() {
        String viewName = indexController.index();
        assertEquals("index", viewName);
    }
}
