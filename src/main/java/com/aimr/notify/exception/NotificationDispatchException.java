package com.aimr.notify.exception;


public class NotificationDispatchException extends RuntimeException implements AbstractException{

    Integer statusCode;

    public NotificationDispatchException(String message) {
        super(message);
    }
    public NotificationDispatchException(final String message, final Integer statusCode){
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
