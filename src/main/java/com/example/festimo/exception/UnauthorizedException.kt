package com.example.festimo.exception;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED_EXCEPTION);
    }
}
