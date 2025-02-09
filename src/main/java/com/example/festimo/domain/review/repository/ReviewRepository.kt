package com.example.festimo.domain.review.repository

import com.example.festimo.domain.review.domain.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByRevieweeId(revieweeId: Long): List<Review>
    fun findByReviewerId(reviewerId: Long): List<Review>

    // 페이징 및 정렬
    fun findByRevieweeId(revieweeId: Long, pageable: Pageable): Page<Review>
    fun findByReviewerId(reviewerId: Long, pageable: Pageable): Page<Review>

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.revieweeId = :userId")
    fun findAverageRatingByRevieweeId(@Param("userId") userId: Long): Double?
}
