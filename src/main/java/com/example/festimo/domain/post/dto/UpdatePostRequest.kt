package com.example.festimo.domain.post.dto;

import com.example.festimo.domain.post.entity.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class UpdatePostRequest {

    @Size(max = 30, message = "제목은 최대 30자까지 입력 가능합니다.")
    private String title;

    private String content;
    private PostCategory category;

    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    private String password;

    public UpdatePostRequest(String title, String content, PostCategory category, String password) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.password = password;
    }
}
