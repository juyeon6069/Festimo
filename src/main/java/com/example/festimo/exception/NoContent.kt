package com.example.festimo.exception;

public class NoContent extends CustomException {
    public NoContent() {
        super(ErrorCode.NO_CONTENT);
    }
}