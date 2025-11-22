package com.example.smartparking.domain;

public class ConflictException extends RuntimeException{
    public ConflictException(String message) {
        super(message);
    }
}
