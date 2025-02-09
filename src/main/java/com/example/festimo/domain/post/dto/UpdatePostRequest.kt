package com.example.festimo.domain.post.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile

data class UpdatePostRequest(
    @field:Size(max = 30, message = "제목은 최대 30자까지 입력 가능합니다.")
    val title: String? = null,
    val content: String? = null,
    val image: MultipartFile? = null,
    val removeImage: Boolean = false,
    val category: String? = null,
    @field:NotBlank(message = "비밀번호는 필수 항목입니다.")
    val password: String = ""
)