package com.example.festimo.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentResponse {
    private Integer sequence;
    private String comment;
    private String nickname;
    private Long postId;
    private boolean isOwner;
    private boolean isAdmin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonIgnore
    private Long id;

    public CommentResponse(Integer sequence, String comment, String nickname, Long postId, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isOwner, boolean isAdmin) {
        this.sequence = sequence;
        this.comment = comment;
        this.nickname = nickname;
        this.postId = postId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isOwner = isOwner;
        this.isAdmin = isAdmin;
    }
}