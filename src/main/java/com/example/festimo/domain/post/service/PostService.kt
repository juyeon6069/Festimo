package com.example.festimo.domain.post.service;

import com.example.festimo.domain.post.dto.*;
import com.example.festimo.global.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PostService {

    // 게시글 등록
    void createPost(@Valid PostRequest request, Authentication authentication);

    // 전체 게시글 조회
    PageResponse<PostListResponse> getAllPosts(int page, int size);

    // 게시글 상세 조회
    PostDetailResponse getPostById(Long postId, boolean incrementView, Authentication authentication);

    // 게시글 수정
    PostDetailResponse updatePost(Long postId, @Valid UpdatePostRequest request);

    // 게시글 삭제
    void deletePost(Long postId, String password, Authentication authentication);

    // 주간 인기 글
    void clearWeeklyTopPostsCache();
    List<PostListResponse> getCachedWeeklyTopPosts();

    // 게시글 검색
    List<PostListResponse> searchPosts(String keyword);

    // 좋아요
    PostDetailResponse toggleLike(Long postId, Authentication authentication);

    // 댓글 목록 조회
    List<CommentResponse> getComments(Long postId);

    // 댓글 등록
    CommentResponse createComment(Long postId, @Valid CommentRequest commentDto, Authentication authentication);

    // 댓글 수정
    CommentResponse updateComment(Long postId, Integer sequence, @Valid UpdateCommentRequest commentDto, Authentication authentication);

    // 댓글 삭제
    void deleteComment(Long postId, Integer sequence, Authentication authentication);
}