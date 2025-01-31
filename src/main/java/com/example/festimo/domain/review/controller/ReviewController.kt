package com.example.festimo.domain.review.controller

import com.example.festimo.domain.review.dto.ReviewRequestDTO
import com.example.festimo.domain.review.dto.ReviewResponseDTO
import com.example.festimo.domain.review.dto.ReviewUpdateDTO
import com.example.festimo.domain.review.service.ReviewService
import com.example.festimo.domain.user.repository.UserRepository
import com.example.festimo.domain.user.service.UserService
import com.example.festimo.exception.CustomException
import com.example.festimo.exception.ErrorCode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@Tag(name = "리뷰 API", description = "리뷰 정보를 관리하는 API")
@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService,
    private val userService: UserService,
    private val userRepository: UserRepository
) {

//    @Operation(summary = "리뷰 작성")
//    @PostMapping
//    fun createReview(
//        authentication: Authentication,
//        @RequestBody @Valid requestDTO: ReviewRequestDTO
//    ): ResponseEntity<String> {
//        println("Received Raw JSON: $requestDTO")
//        val email = authentication.name
//        println("Authenticated User Email: $email")
//        val reviewerId = userRepository.findUserIdByEmail(email)
//            .orElseThrow { IllegalArgumentException("User not found with email: $email") }
//
//        //requestDTO.reviewerId = reviewerId
//        val updatedRequestDTO = requestDTO.copy(reviewerId = reviewerId)
//        println("Updated RequestDTO: $updatedRequestDTO")
//
//
//        val message = reviewService.createReview(updatedRequestDTO)
//        return ResponseEntity.status(HttpStatus.CREATED).body(message)
//    }

    @Operation(summary = "리뷰 작성")
    @PostMapping
    fun createReview(
        authentication: Authentication,
        @RequestBody rawJson: String // JSON 데이터 수동으로 받기
    ): ResponseEntity<String> {
        println("Received Raw JSON: $rawJson")

        val objectMapper = jacksonObjectMapper()
        val requestDTO = objectMapper.readValue(rawJson, ReviewRequestDTO::class.java)
        println("Mapped DTO: $requestDTO")

        val email = authentication.name
        val reviewerId = userService.getUserIdByEmail(email)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val updatedRequestDTO = requestDTO.copy(reviewerId = reviewerId)
        val message = reviewService.createReview(updatedRequestDTO)
        return ResponseEntity.status(HttpStatus.CREATED).body(message)
    }

    @Operation(summary = "리뷰 조회")
    @GetMapping("/reviewee/{revieweeId}")
    fun getReviewsForUser(@PathVariable revieweeId: Long): ResponseEntity<List<ReviewResponseDTO>> {
        val reviews = reviewService.getReviewsForUser(revieweeId)
        return ResponseEntity.ok(reviews)
    }

    @Operation(summary = "페이징 및 정렬")
    @GetMapping("/reviewee/{revieweeId}/paged")
    fun getPagedReviews(
        @PathVariable revieweeId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String
    ): ResponseEntity<Page<ReviewResponseDTO>> {
        val sort = if (sortDir.equals("asc", ignoreCase = true)) Sort.by(sortBy).ascending()
        else Sort.by(sortBy).descending()

        val pageable = PageRequest.of(page, size, sort)
        val reviews = reviewService.getPagedReviewsForUser(revieweeId, pageable)
        return ResponseEntity.ok(reviews)
    }

    @Operation(summary = "내가 받은 리뷰 페이징 조회 - 마이페이지")
    @GetMapping("/reviewee/mypage/paged")
    fun getPagedReviewsForAuthenticatedUser(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String
    ): ResponseEntity<Page<ReviewResponseDTO>> {
        val email = authentication.name
        val revieweeId = userService.getUserIdByEmail(email)

        val sort = if (sortDir.equals("asc", ignoreCase = true)) Sort.by(sortBy).ascending()
        else Sort.by(sortBy).descending()

        val pageable = PageRequest.of(page, size, sort)
        val reviews = reviewService.getPagedReviewsForUser(revieweeId, pageable)
        return ResponseEntity.ok(reviews)
    }

    @Operation(summary = "내가 쓴 리뷰 조회")
    @GetMapping("/reviewer/{reviewerId}")
    fun getReviewsByReviewer(@PathVariable reviewerId: Long): ResponseEntity<List<ReviewResponseDTO>> {
        val reviews = reviewService.getReviewsByReviewer(reviewerId)
        return ResponseEntity.ok(reviews)
    }

    @Operation(summary = "내가 쓴 리뷰 페이징 조회 - 마이페이지")
    @GetMapping("/reviewer/mypage/paged")
    fun getPagedReviewsForReviewer(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String
    ): ResponseEntity<Page<ReviewResponseDTO>> {
        val email = authentication.name
        val reviewerId = userService.getUserIdByEmail(email)
        println("Authenticated email: $email")
        println("Reviewer ID: $reviewerId")


        val sort = if (sortDir.equals("asc", ignoreCase = true)) Sort.by(sortBy).ascending()
        else Sort.by(sortBy).descending()

        val pageable = PageRequest.of(page, size, sort)
        val reviews = reviewService.getPagedReviewsByReviewer(reviewerId, pageable)
        println("Reviews: $reviews")

        return ResponseEntity.ok(reviews)
    }

    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("/{reviewId}")
    fun deleteReview(@PathVariable reviewId: Long): ResponseEntity<String> {
        val message = reviewService.deleteReview(reviewId)
        return ResponseEntity.ok(message)
    }

    @Operation(summary = "리뷰 수정")
    @PutMapping("/{reviewId}")
    fun updateReview(
        @PathVariable reviewId: Long,
        @RequestBody @Valid updateDTO: ReviewUpdateDTO
    ): ResponseEntity<String> {
        val message = reviewService.updateReview(reviewId, updateDTO)
        return ResponseEntity.ok(message)
    }
}