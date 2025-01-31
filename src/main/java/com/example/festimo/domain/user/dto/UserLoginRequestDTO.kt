package com.example.festimo.domain.user.dto

import lombok.Getter
import lombok.Setter

@Getter
@Setter
data class UserLoginRequestDTO (
    val email: String = "",
    val password: String = ""
)