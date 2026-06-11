package com.aimr.notify.exception;


public class DataTransportException extends RuntimeException implements AbstractException{

    Integer statusCode;

    public DataTransportException(String message) {
        super(message);
    }
    public DataTransportException(final String message, final Integer statusCode){
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
