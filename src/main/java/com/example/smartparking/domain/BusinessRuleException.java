package com.example.smartparking.domain;

public class BusinessRuleException extends RuntimeException{
    private final String errorCode;
    public BusinessRuleException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    public String errorCode() {
        return errorCode;
    }
}
