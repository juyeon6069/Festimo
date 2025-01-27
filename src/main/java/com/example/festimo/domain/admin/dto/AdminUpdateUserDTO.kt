package com.example.festimo.domain.admin.dto

import jakarta.validation.constraints.*

data class AdminUpdateUserDTO(
    @field:NotNull(message = "이름은 필수 입력 값입니다.")
    @field:NotBlank
    var userName: String = "",

    @field:NotNull(message = "닉네임은 필수 입력 값입니다.")
    @field:NotBlank
    var nickname: String = "",

    @field:NotNull(message = "이메일은 필수 입력 값입니다.")
    @field:NotBlank
    var email: String = "",

    @field:NotNull(message = "성별은 필수 입력 값입니다.")
    @field:NotBlank
    @field:Pattern(regexp = "^(M|F)$", message = "성별은 F 또는 M만 가능합니다.")
    var gender: String = "",

    @field:NotNull(message = "평점은 필수 입력 값입니다.")
    @field:Min(value = 0, message = "평점은 0 이상이어야 합니다.")
    @field:Max(value = 5, message = "평점은 5 이하여야 합니다.")
    var ratingAvg: Float = 0f
)