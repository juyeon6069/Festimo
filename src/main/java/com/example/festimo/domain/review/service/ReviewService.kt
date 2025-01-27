package com.example.festimo.domain.review.service

import com.example.festimo.domain.review.domain.Review
import com.example.festimo.domain.review.dto.ReviewRequestDTO
import com.example.festimo.domain.review.dto.ReviewResponseDTO
import com.example.festimo.domain.review.dto.ReviewUpdateDTO
import com.example.festimo.domain.review.repository.ReviewRepository
import com.example.festimo.domain.user.service.UserService
import com.example.festimo.exception.CustomException
import com.example.festimo.exception.ErrorCode
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val userService: UserService
) {
    // 리뷰 생성
    @Transactional
    fun createReview(requestDTO: ReviewRequestDTO): String {
        return try {
            // DTO -> 엔티티 매핑
            val review = Review(
                reviewerId = requestDTO.reviewerId,
                revieweeId = requestDTO.revieweeId,
                content = requestDTO.content,
                rating = requestDTO.rating,
                applicationId = requestDTO.applicationId,
                companyId2 = requestDTO.companyId2,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            println("Review Entity Before Save: $review")

            reviewRepository.save(review)
            println("Review Entity After Save")

            // 평점 평균 업데이트
            userService.updateUserRatingAvg(requestDTO.revieweeId)

            "Review created successfully."
        } catch (e: Exception) {
            throw RuntimeException("Failed to create review. Reason: ${e.message}")
        }
    }

    // 특정 대상자의 리뷰 조회
    fun getReviewsForUser(revieweeId: Long): List<ReviewResponseDTO> {
        val reviews = reviewRepository.findByRevieweeId(revieweeId)
        if (reviews.isEmpty()) throw CustomException(ErrorCode.REVIEW_NOT_FOUND)

        // 엔티티 -> DTO 매핑
        return reviews.map { mapToResponseDTO(it) }
    }

    // 특정 대상자의 리뷰 조회 (페이징)
    fun getPagedReviewsForUser(revieweeId: Long, pageable: Pageable): Page<ReviewResponseDTO> {
        val reviewPage = reviewRepository.findByRevieweeId(revieweeId, pageable)
        if (reviewPage.isEmpty) throw CustomException(ErrorCode.REVIEW_NOT_FOUND)

        // 엔티티 -> DTO 매핑
        return reviewPage.map { mapToResponseDTO(it) }
    }

    // 특정 리뷰어의 리뷰 조회
    fun getReviewsByReviewer(reviewerId: Long): List<ReviewResponseDTO> {
        val reviews = reviewRepository.findByReviewerId(reviewerId)
        if (reviews.isEmpty()) throw CustomException(ErrorCode.REVIEW_NOT_FOUND)

        // 엔티티 -> DTO 매핑
        return reviews.map { mapToResponseDTO(it) }
    }

    // 특정 리뷰어의 리뷰 조회 (페이징)
    fun getPagedReviewsByReviewer(reviewerId: Long, pageable: Pageable): Page<ReviewResponseDTO> {
        val reviewPage = reviewRepository.findByReviewerId(reviewerId, pageable)
        if (reviewPage.isEmpty) throw CustomException(ErrorCode.REVIEW_NOT_FOUND)

        // 엔티티 -> DTO 매핑
        return reviewPage.map { mapToResponseDTO(it) }
    }

    // 리뷰 삭제
    @Transactional
    fun deleteReview(reviewId: Long): String {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { CustomException(ErrorCode.REVIEW_NOT_FOUND) }

        val revieweeId = review.revieweeId
        reviewRepository.delete(review)

        // 평점 평균 업데이트
        userService.updateUserRatingAvg(revieweeId)
        return "Review deleted successfully."
    }

    // 리뷰 수정
    @Transactional
    fun updateReview(reviewId: Long, updateDTO: ReviewUpdateDTO): String {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { CustomException(ErrorCode.REVIEW_NOT_FOUND) }

        // 수정 가능한 필드 업데이트
        review.rating = updateDTO.rating
        review.content = updateDTO.content
        review.updatedAt = LocalDateTime.now()

        reviewRepository.save(review)
        return "Review updated successfully."
    }

    // 엔티티 -> DTO 매핑 함수
    private fun mapToResponseDTO(review: Review): ReviewResponseDTO {
        return ReviewResponseDTO(
            reviewId = review.reviewId ?: 0L,
            reviewerId = review.reviewerId ?: 0L,
            revieweeId = review.revieweeId,
            content = review.content,
            rating = review.rating,
            createdAt = review.createdAt ?: LocalDateTime.now()
        )
    }
}
