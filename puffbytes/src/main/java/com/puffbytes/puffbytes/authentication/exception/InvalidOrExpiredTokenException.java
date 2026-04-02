package com.puffbytes.puffbytes.authentication.exception;

public class InvalidOrExpiredTokenException extends RuntimeException {
    public InvalidOrExpiredTokenException(String message) {
        super(message);
    }
}
