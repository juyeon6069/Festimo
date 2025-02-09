package com.example.festimo.domain.meet.dto

data class ApplicantReviewResponse(
    var rating: Int,    // 평점
    var content: String // 리뷰 내용
)