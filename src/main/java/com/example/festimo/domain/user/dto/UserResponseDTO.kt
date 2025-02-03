package com.example.festimo.domain.user.dto

data class UserResponseDTO (
    var id: Long? = null,
    var userName: String? = null,
    var nickname: String? = null,
    var email: String? = null,
    var role: String? = null,
    var gender: String? = null,
    var ratingAvg: Float? = null
)
