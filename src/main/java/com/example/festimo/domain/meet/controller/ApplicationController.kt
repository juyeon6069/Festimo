package com.example.festimo.domain.meet.controller

import com.example.festimo.domain.meet.dto.ApplicantReviewResponse
import com.example.festimo.domain.meet.dto.ApplicationRequest
import com.example.festimo.domain.meet.dto.ApplicationResponse
import com.example.festimo.domain.meet.dto.LeaderApplicationResponse
import com.example.festimo.domain.meet.service.ApplicationService
import com.example.festimo.global.utils.jwt.JwtTokenProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable



@RestController
@RequestMapping("/api/meet")
@Tag(name = "동행 API", description = "동행 관련 API")
class ApplicationController(
    private val applicationService: ApplicationService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @PostMapping("/applications")
    @Operation(summary = "동행 신청")
    fun apply(
        @RequestHeader("Authorization") authorizationHeader: String,
        @RequestBody request: ApplicationRequest
    ): ResponseEntity<ApplicationResponse> {
        val email = getEmailFromHeader(authorizationHeader)
        val response = applicationService.createApplication(email, request.companionId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/companion/{companionId}")
    @Operation(summary = "리더의 동행 신청 리스트")
    fun getAllApplications(
        @RequestHeader("Authorization") authorizationHeader: String,
        @PathVariable companionId: Long
    ): ResponseEntity<List<LeaderApplicationResponse>> {
        val email = getEmailFromHeader(authorizationHeader)
        val responses = applicationService.getAllApplications(companionId, email)
        return ResponseEntity.status(HttpStatus.OK).body(responses)
    }

    @PostMapping("/{applicationId}/accept")
    @Operation(summary = "리더의 동행 신청 승인")
    fun acceptApplication(
        @RequestHeader("Authorization") authorizationHeader: String,
        @PathVariable applicationId: Long
    ): ResponseEntity<Void> {
        val email = getEmailFromHeader(authorizationHeader)
        applicationService.acceptApplication(applicationId, email)
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/{applicationId}/reject")
    @Operation(summary = "리더의 동행 신청 거절")
    fun rejectApplication(
        @RequestHeader("Authorization") authorizationHeader: String,
        @PathVariable applicationId: Long
    ): ResponseEntity<Void> {
        val email = getEmailFromHeader(authorizationHeader)
        applicationService.rejectApplication(applicationId, email)
        return ResponseEntity.ok().build()
    }

    /**
     * 신청자 리뷰 확인
     *
     * @param applicationId 확인하고 싶은 신청자의 신청 ID
     * @param page
     */
    @GetMapping("/{applicationId}/reviews")
    fun getApplicantReview(
        @PathVariable applicationId: Long,
        page: Int
    ): ResponseEntity<Page<ApplicantReviewResponse>> {
        val pageable: Pageable = PageRequest.of(page, 5, Sort.by("createdAt").descending())
        val reviews: Page<ApplicantReviewResponse> = applicationService.getApplicantReviews(applicationId, pageable)

        if (reviews.isEmpty) {
            return ResponseEntity.noContent().build()  // 리뷰가 없을 경우
        }

        return ResponseEntity.ok(reviews)
    }

    private fun getEmailFromHeader(authorizationHeader: String): String {
        val token = authorizationHeader.replace("Bearer ", "")
        return jwtTokenProvider.getEmailFromToken(token)
    }
}