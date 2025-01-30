package com.example.festimo.domain.review.domain

import jakarta.persistence.*
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Table(name = "Reviews")
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val reviewId: Long? = null,

    @Column(nullable = true) // NULL 허용
    val reviewerId: Long? = null, // 작성자

    @Column(nullable = false)
    val revieweeId: Long, // 대상자

    @field:Min(1)
    @field:Max(5)
    @Column(nullable = false)
    var rating: Int, // 평점 숫자

    @Column(nullable = false, length = 255)
    var content: String, // 리뷰 내용

    @CreatedDate
    val createdAt: LocalDateTime? = null, // 생성일 (자동 관리)

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null, // 수정일 (자동 관리)

    @Column(nullable = true) // NULL 허용
    val applicationId: Long? = null,

    @Column(nullable = true) // NULL 허용
    val companyId2: Long? = null

)