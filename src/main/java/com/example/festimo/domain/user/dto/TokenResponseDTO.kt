package com.example.festimo.domain.user.dto

data class TokenResponseDTO (
    var accessToken: String? = null,

    var refreshToken: String? = null,

    var nickname: String? = null,

    var email: String? = null

)