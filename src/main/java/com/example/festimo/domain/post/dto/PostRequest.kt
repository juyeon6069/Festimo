package com.example.festimo.domain.post.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile

data class PostRequest(
    @field:NotBlank(message = "제목은 필수 입력 항목입니다.")
    @field:Size(max = 30, message = "제목은 30자 이하로 입력해주세요.")
    val title: String = "",

    @field:NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @field:Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하로 입력해주세요.")
    val password: String = "",

    @field:NotBlank(message = "내용은 필수 입력 항목입니다.")
    val content: String = "",

    val image: MultipartFile? = null,
    val category: String = "",
    val tags: List<String>? = null
)