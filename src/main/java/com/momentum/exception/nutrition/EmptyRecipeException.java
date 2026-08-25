package com.momentum.exception.nutrition;

public class EmptyRecipeException extends RuntimeException {
    public EmptyRecipeException(String message) {
        super(message);
    }
}

