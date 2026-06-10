package com.aimr.notify.exception;

public class ConnectionTimeoutException extends RuntimeException implements AbstractException{

    Integer statusCode;
    public ConnectionTimeoutException(String message) {
        super(message);
    }

    public ConnectionTimeoutException(String message, Integer statusCode){
        super(message);
        this.statusCode=statusCode;
    }

    @Override
    public Integer getStatusCode(){
        return statusCode;
    }

    @Override
    public String getErrorMessage(){
        return getMessage();
    }
}
