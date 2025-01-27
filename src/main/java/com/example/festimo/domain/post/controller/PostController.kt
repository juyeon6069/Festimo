package com.example.festimo.domain.post.controller

import com.example.festimo.domain.post.dto.*
import com.example.festimo.domain.post.entity.PostCategory
import com.example.festimo.domain.post.service.PostService
import com.example.festimo.global.dto.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*
import java.beans.PropertyEditorSupport

@Tag(name = "Post")
@RestController
@RequestMapping("/api/companions")
class PostController(private val postService: PostService) {

    @InitBinder
    fun initBinder(binder: WebDataBinder) {
        binder.registerCustomEditor(PostCategory::class.java, object : PropertyEditorSupport() {
            override fun setAsText(text: String) {
                value = PostCategory.fromDisplayName(text)
            }
        })
    }

    @Operation(summary = "게시글 등록")
    @PostMapping
    fun createPost(
        @RequestBody request: PostRequest,
        authentication: Authentication
    ): ResponseEntity<Void> {
        postService.createPost(request, authentication)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @Operation(summary = "게시글 전체 조회")
    @GetMapping
    fun getAllPosts(
        @RequestParam(value = "page", defaultValue = "1") page: Int,
        @RequestParam(value = "size", defaultValue = "10") size: Int
    ): ResponseEntity<PageResponse<PostListResponse>> =
        ResponseEntity.ok(postService.getAllPosts(page, size))

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/{postId}")
    fun getPostById(
        @PathVariable postId: Long,
        @RequestParam(name = "view", required = false, defaultValue = "false") incrementView: Boolean,
        authentication: Authentication
    ): ResponseEntity<PostDetailResponse> =
        ResponseEntity.ok(postService.getPostById(postId, incrementView, authentication))

    @Operation(summary = "게시글 수정")
    @PutMapping("/{postId}")
    fun updatePost(
        @PathVariable postId: Long,
        @RequestBody request: UpdatePostRequest
    ): ResponseEntity<PostDetailResponse> =
        ResponseEntity.ok(postService.updatePost(postId, request))

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{postId}")
    fun deletePost(
        @PathVariable postId: Long,
        @RequestBody request: DeletePostRequest,
        authentication: Authentication
    ): ResponseEntity<Void> {
        postService.deletePost(postId, request.password, authentication)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "주간 인기 게시물 조회")
    @GetMapping("/top-weekly")
    fun getWeeklyTopPosts(): ResponseEntity<List<PostListResponse>> =
        ResponseEntity.ok(postService.getCachedWeeklyTopPosts())

    @Operation(summary = "키워드로 게시글 검색")
    @GetMapping("/search")
    fun searchPosts(@RequestParam keyword: String): ResponseEntity<List<PostListResponse>> =
        ResponseEntity.ok(postService.searchPosts(keyword))

    @Operation(summary = "게시글 좋아요")
    @PostMapping("/{postId}/like")
    fun toggleLike(
        @PathVariable postId: Long,
        authentication: Authentication
    ): ResponseEntity<PostDetailResponse> =
        ResponseEntity.ok(postService.toggleLike(postId, authentication))

    @Operation(summary = "댓글 목록 조회")
    @GetMapping("/{postId}/comments")
    fun getComments(@PathVariable postId: Long): ResponseEntity<List<CommentResponse>> =
        ResponseEntity.ok(postService.getComments(postId))

    @Operation(summary = "댓글 등록")
    @PostMapping("/{postId}/comments")
    fun createComment(
        @PathVariable postId: Long,
        @RequestBody @Valid request: CommentRequest,
        authentication: Authentication
    ): ResponseEntity<CommentResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(postService.createComment(postId, request, authentication))

    @Operation(summary = "댓글 수정")
    @PutMapping("/{postId}/comments/{sequence}")
    fun updateComment(
        @PathVariable postId: Long,
        @PathVariable sequence: Int,
        @RequestBody @Valid request: UpdateCommentRequest,
        authentication: Authentication
    ): ResponseEntity<CommentResponse> =
        ResponseEntity.ok(postService.updateComment(postId, sequence, request, authentication))

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{postId}/comments/{sequence}")
    fun deleteComment(
        @PathVariable postId: Long,
        @PathVariable sequence: Int,
        authentication: Authentication
    ): ResponseEntity<Void> {
        postService.deleteComment(postId, sequence, authentication)
        return ResponseEntity.noContent().build()
    }
}