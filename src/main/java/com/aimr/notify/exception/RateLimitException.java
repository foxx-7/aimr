package com.aimr.notify.exception;

/**
 * Thrown when a tenant has exceeded the allowed notification
 * request rate for a given channel within the current hour window.
 *
 * Maps to HTTP 429 TOO MANY REQUESTS.
 */
public class RateLimitException extends RuntimeException implements AbstractException{

    private int statusCode;
    public RateLimitException(String message) {
        super(message);
    }
    public RateLimitException(String message, int statusCode){
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
