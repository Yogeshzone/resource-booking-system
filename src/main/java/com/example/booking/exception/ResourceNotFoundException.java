package com.example.booking.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Long resourceId) {
        super("Resource not found with ID: " + resourceId);
    }
}
