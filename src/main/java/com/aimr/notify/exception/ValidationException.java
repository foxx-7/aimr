package com.aimr.notify.exception;

public class ValidationException extends RuntimeException implements AbstractException {

    Integer statusCode;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    @Override
    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public String getErrorMessage() {
        return getMessage();
    }
}
