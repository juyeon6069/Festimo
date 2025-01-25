package com.example.festimo.domain.post.repository

import com.example.festimo.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface PostRepository : JpaRepository<Post, Long> {

    // 게시글 상세 정보 조회
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.comments LEFT JOIN FETCH p.tags WHERE p.id = :postId")
    fun findByIdWithDetails(@Param("postId") postId: Long): Optional<Post>

    // 조회 수 증가
    @Modifying
    @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.id = :id")
    fun incrementViews(@Param("id") id: Long)

    // 특정 날짜 이후에 생성된 게시글 조회
    fun findByCreatedAtAfter(date: LocalDateTime): List<Post>

    // 인기 게시글
    @Query("""
       SELECT DISTINCT p FROM Post p 
       LEFT JOIN FETCH p.tags 
       WHERE p.createdAt >= :lastWeek 
       AND (p.likes >= 5 OR p.replies >= 10 OR p.views >= 30) 
       ORDER BY (p.likes * 3 + p.replies * 2 + p.views * 0.1) DESC
   """)
    fun findTopPostsOfWeek(@Param("lastWeek") lastWeek: LocalDateTime): List<Post>

    // 게시글 검색
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    fun searchPostsByKeyword(@Param("keyword") keyword: String): List<Post>

    // 태그로 검색
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t LIKE %:tag%")
    fun findByTag(@Param("tag") tag: String): List<Post>

}