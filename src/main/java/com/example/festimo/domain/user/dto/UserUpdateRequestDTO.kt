package com.example.festimo.domain.user.dto

data class UserUpdateRequestDTO (
    val nickname: String? = null, // 닉네임
    val userName: String? = null,// 사용자 이름
    val gender: String? = null // 성별
)
