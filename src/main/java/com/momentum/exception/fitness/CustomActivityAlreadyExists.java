package com.momentum.exception.fitness;

public class CustomActivityAlreadyExists extends RuntimeException {
    public CustomActivityAlreadyExists(String message) {
        super(message);
    }
}

