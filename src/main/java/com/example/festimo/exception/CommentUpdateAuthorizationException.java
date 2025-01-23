package com.example.festimo.exception;

public class CommentUpdateAuthorizationException extends CustomException {
    public CommentUpdateAuthorizationException() {
        super(ErrorCode.COMMENT_UPDATE_AUTHORIZATION_EXCEPTION);
    }
}
