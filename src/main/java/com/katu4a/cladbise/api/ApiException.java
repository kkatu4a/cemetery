package com.katu4a.cladbise.api;

public class ApiException extends RuntimeException {
    private final int status;
    public ApiException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
    public int status() { return status; }
}
