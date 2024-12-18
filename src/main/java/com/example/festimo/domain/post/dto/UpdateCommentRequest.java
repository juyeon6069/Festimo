package com.example.festimo.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateCommentRequest {
    @NotBlank(message = "댓글은 필수 입력 항목입니다.")
    private String comment;
}