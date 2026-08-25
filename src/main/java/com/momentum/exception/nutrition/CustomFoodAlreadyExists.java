package com.momentum.exception.nutrition;

public class CustomFoodAlreadyExists extends RuntimeException
{
    public CustomFoodAlreadyExists(String message) {
        super(message);
    }
}

