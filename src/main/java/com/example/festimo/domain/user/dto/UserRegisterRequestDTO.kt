package com.example.festimo.domain.user.dto

import lombok.Getter
import lombok.Setter

@Getter
@Setter
data class UserRegisterRequestDTO (
    val userName: String? = null,
    val nickname: String? = null,
    val email: String? = null,
    val password: String = "",
    val gender: String? = null
)