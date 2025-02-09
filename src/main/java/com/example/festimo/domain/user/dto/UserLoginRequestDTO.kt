package com.example.festimo.domain.user.dto

data class UserLoginRequestDTO (
    val email: String = "",
    val password: String = ""
)