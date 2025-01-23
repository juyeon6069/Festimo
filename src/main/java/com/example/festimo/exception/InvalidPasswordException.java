package com.example.festimo.exception;

public class InvalidPasswordException extends CustomException {
    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD_EXCEPTION);
    }
}