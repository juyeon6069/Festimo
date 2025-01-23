package com.example.festimo.domain.post.dto;

import com.example.festimo.domain.post.entity.PostCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDetailResponse {
    private Long id;
    private String title;
    private String nickname;
    private String avatar;
    private String mail;
    private String content;
    private PostCategory category;
    private int views;
    private int replies;
    private int likes;
    private Set<String> tags;
    private String createdAt;
    private String updatedAt;
    private boolean isOwner;
    private boolean isAdmin;
    private boolean isLiked;
    private List<CommentResponse> comments = new ArrayList<>();
}