package com.example.festimo.domain.user.dto

import lombok.*

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
data class TokenResponseDTO (
    var accessToken: String? = null,

    var refreshToken: String? = null,

    var nickname: String? = null,

    var email: String? = null

)