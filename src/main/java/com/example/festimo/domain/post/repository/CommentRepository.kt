package com.example.festimo.domain.post.repository

import com.example.festimo.domain.post.entity.Comment
import com.example.festimo.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CommentRepository : JpaRepository<Comment, Long> {

    // 게시글별 댓글의 최대 sequence 조회
    @Query("SELECT COALESCE(MAX(c.sequence), 0) FROM Comment c WHERE c.post = :post")
    fun findMaxSequenceByPost(@Param("post") post: Post): Int?

    // 게시글과 sequence를 기준으로 댓글 찾기
    fun findByPostIdAndSequence(postId: Long, sequence: Int): Comment?

    // 게시글 별 댓글 오름차순 조회
    fun findByPostOrderBySequenceAsc(post: Post): List<Comment>

}