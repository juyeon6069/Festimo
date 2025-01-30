package com.example.festimo.domain.post.service

import com.example.festimo.domain.post.dto.*
import com.example.festimo.global.dto.PageResponse
import jakarta.validation.Valid
import org.springframework.security.core.Authentication

interface PostService {
    // 게시글 등록
    fun createPost(@Valid request: PostRequest, authentication: Authentication)

    // 전체 게시글 조회
    fun getAllPosts(page: Int, size: Int): PageResponse<PostListResponse>

    // 게시글 상세 조회
    fun getPostById(postId: Long, incrementView: Boolean, authentication: Authentication): PostDetailResponse

    // 게시글 수정
    fun updatePost(postId: Long, @Valid request: UpdatePostRequest): PostDetailResponse

    // 게시글 삭제
    fun deletePost(postId: Long, password: String, authentication: Authentication)

    // 주간 인기 글
    fun clearWeeklyTopPostsCache()
    fun getCachedWeeklyTopPosts(): List<PostListResponse>

    // 게시글 검색
    fun searchPosts(keyword: String): List<PostListResponse>

    // 좋아요
    fun toggleLike(postId: Long, authentication: Authentication): PostDetailResponse

    // 댓글 목록 조회
    fun getComments(postId: Long): List<CommentResponse>

    // 댓글 등록
    fun createComment(postId: Long, @Valid request: CommentRequest, authentication: Authentication): CommentResponse

    // 댓글 수정
    fun updateComment(postId: Long, sequence: Int, @Valid request: UpdateCommentRequest, authentication: Authentication): CommentResponse

    // 댓글 삭제
    fun deleteComment(postId: Long, sequence: Int, authentication: Authentication)
}