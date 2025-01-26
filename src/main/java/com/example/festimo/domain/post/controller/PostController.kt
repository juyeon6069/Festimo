package com.example.festimo.domain.post.controller;

import com.example.festimo.domain.post.dto.*;
import com.example.festimo.domain.post.entity.PostCategory;
import com.example.festimo.domain.post.service.PostService;
import com.example.festimo.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditorSupport;
import java.util.List;

@Tag(name = "Post")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companions")
public class PostController {

    private final PostService postService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(PostCategory.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                setValue(PostCategory.fromDisplayName(text));
            }
        });
    }

    @Operation(summary = "게시글 등록")
    @PostMapping
    public ResponseEntity<Void> createPost(
            @RequestBody PostRequest request,
            Authentication authentication
    ) {
        postService.createPost(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "게시글 전체 조회")
    @GetMapping
    public ResponseEntity<PageResponse<PostListResponse>> getAllPosts(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PageResponse<PostListResponse> allPosts = postService.getAllPosts(page, size);
        return ResponseEntity.status(HttpStatus.OK).body(allPosts);
    }

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostById(
            @PathVariable("postId") Long postId,
            @RequestParam(name = "view", required = false, defaultValue = "false") boolean incrementView,
            Authentication authentication) {
        PostDetailResponse postById = postService.getPostById(postId, incrementView, authentication);
        return ResponseEntity.status(HttpStatus.OK).body(postById);
    }

    @Operation(summary = "게시글 수정")
    @PutMapping(value = "/{postId}")
    public ResponseEntity<PostDetailResponse> updatePost(
            @PathVariable Long postId,
            @RequestBody UpdatePostRequest request
    ) {
        PostDetailResponse updatePost = postService.updatePost(postId, request);
        return ResponseEntity.status(HttpStatus.OK).body(updatePost);
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestBody DeletePostRequest request,
            Authentication authentication) {
        postService.deletePost(postId, request.getPassword(), authentication);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "주간 인기 게시물 조회")
    @GetMapping("/top-weekly")
    public ResponseEntity<List<PostListResponse>> getWeeklyTopPosts() {
        List<PostListResponse> topPosts = postService.getCachedWeeklyTopPosts();
        return ResponseEntity.ok(topPosts);
    }

    @Operation(summary = "키워드로 게시글 검색")
    @GetMapping("/search")
    public ResponseEntity<List<PostListResponse>> searchPosts(@RequestParam String keyword) {
        List<PostListResponse> posts = postService.searchPosts(keyword);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "게시글 좋아요")
    @PostMapping("/{postId}/like")
    public ResponseEntity<PostDetailResponse> toggleLike(@PathVariable("postId") Long postId, Authentication authentication) {
        PostDetailResponse postDetailResponse = postService.toggleLike(postId, authentication);
        return ResponseEntity.ok(postDetailResponse);
    }

    @Operation(summary = "댓글 목록 조회")
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        List<CommentResponse> comments = postService.getComments(postId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "댓글 등록")
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentRequest request,
            Authentication authentication) {
        CommentResponse comment = postService.createComment(postId, request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/{postId}/comments/{sequence}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Integer sequence,
            @RequestBody @Valid UpdateCommentRequest request,
            Authentication authentication
    ) {
        CommentResponse comment = postService.updateComment(postId, sequence, request, authentication);
        return ResponseEntity.status(HttpStatus.OK).body(comment);
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{postId}/comments/{sequence}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Integer sequence,
            Authentication authentication
    ) {
        postService.deleteComment(postId, sequence, authentication);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}