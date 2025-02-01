package com.example.festimo.domain.user.dto

data class UserRegisterRequestDTO (
    val userName: String = "",
    val nickname: String = "",
    val email: String = "",
    val password: String = "",
    val gender: String = ""
)