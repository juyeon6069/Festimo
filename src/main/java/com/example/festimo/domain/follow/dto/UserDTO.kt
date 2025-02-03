package com.example.festimo.domain.follow.dto

import com.example.festimo.domain.user.domain.User

data class UserDTO(
    val id: Long,
    val nickname: String,
    val email: String
) {
    companion object {
        fun from(user: User): UserDTO {
            return UserDTO(
                id = user.id,
                nickname = user.nickname,
                email = user.email
            )
        }
    }
}
