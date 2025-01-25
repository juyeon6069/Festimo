package com.example.festimo.exception;

public class CommentDeleteAuthorizationException extends CustomException{
    public CommentDeleteAuthorizationException() {
        super(ErrorCode.COMMENT_DELETE_AUTHORIZATION_EXCEPTION);
    }
}
