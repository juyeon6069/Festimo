package com.example.festimo.exception;

public class PostDeleteAuthorizationException extends CustomException{
    public PostDeleteAuthorizationException() {
        super(ErrorCode.POST_DELETE_AUTHORIZATION_EXCEPTION);
    }
}
