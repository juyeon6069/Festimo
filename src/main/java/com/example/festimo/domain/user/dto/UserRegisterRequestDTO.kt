package com.example.festimo.domain.user.dto

import lombok.Getter
import lombok.Setter

@Getter
@Setter
data class UserRegisterRequestDTO (
    val userName: String = "",
    val nickname: String = "",
    val email: String = "",
    val password: String = "",
    val gender: String = ""
)