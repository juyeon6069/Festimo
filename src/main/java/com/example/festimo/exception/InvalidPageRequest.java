package com.example.festimo.exception;

public class InvalidPageRequest extends CustomException {
    public InvalidPageRequest() {
        super(ErrorCode.INVALID_PAGE_REQUEST);
    }
}