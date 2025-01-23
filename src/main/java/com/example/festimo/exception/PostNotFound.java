package com.example.festimo.exception;

public class PostNotFound extends CustomException {
    public PostNotFound() {
        super(ErrorCode.POST_NOT_FOUND);
    }
}