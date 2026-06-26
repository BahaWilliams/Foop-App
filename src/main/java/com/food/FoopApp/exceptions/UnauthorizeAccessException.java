package com.food.FoopApp.exceptions;

public class UnauthorizeAccessException extends RuntimeException {
    public UnauthorizeAccessException(String message) {
        super(message);
    }
}
