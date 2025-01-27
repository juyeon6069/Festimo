package com.example.festimo.domain.user.dto

import lombok.*

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
data class UserResponseDTO (
    var id: Long? = null,
    var userName: String? = null,
    var nickname: String? = null,
    var email: String? = null,
    var role: String? = null,
    var gender: String? = null // rating
)
