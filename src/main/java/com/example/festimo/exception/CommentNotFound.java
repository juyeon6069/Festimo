package com.example.festimo.exception;

public class CommentNotFound extends CustomException {
    public CommentNotFound() {
        super(ErrorCode.COMMENT_NOT_FOUND);
    }
}
