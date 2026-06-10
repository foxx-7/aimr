package com.aimr.notify.exception;

public class DataConversionException extends RuntimeException implements AbstractException{
    int statusCode;
    public DataConversionException(String message) {
        super(message);
    }

    public DataConversionException(String message, int statusCode){
        super(message);
        this.statusCode=statusCode;
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
