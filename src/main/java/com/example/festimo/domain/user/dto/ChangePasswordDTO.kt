package com.example.festimo.domain.user.dto

import jakarta.validation.constraints.NotBlank
import lombok.Getter
import lombok.Setter

@Getter
@Setter
data class ChangePasswordDTO (
    val oldPassword: @NotBlank String = "",

    val newPassword: @NotBlank String = ""
)