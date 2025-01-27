package com.example.festimo.domain.admin.dto

import java.time.LocalDateTime

data class AdminDTO(
    var userId: Long = 0,
    var userName: String = "",
    var nickname: String = "",
    var email: String = "",
    var role: String = "",
    var createdDate: LocalDateTime = LocalDateTime.now(),
    var gender: String = "",
    var ratingAvg: Float = 0f
)