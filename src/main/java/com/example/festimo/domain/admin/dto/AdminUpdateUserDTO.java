package com.example.festimo.domain.admin.dto;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class AdminUpdateUserDTO {

    @NotNull(message = "이름은 필수 입력 값입니다.")
    @NotBlank
    private String userName;

    @NotNull(message = "닉네임은 필수 입력 값입니다.")
    @NotBlank
    private String nickname;

    @NotNull(message = "이메일은 필수 입력 값입니다.")
    @NotBlank
    private String email;

    @NotNull(message = "성별은 필수 입력 값입니다.")
    @NotBlank
    @Pattern(regexp = "^(M|F)$", message = "성별은 F 또는 M만 가능합니다.")
    private String gender; // F, M만 허용

    @NotNull(message = "평점은 필수 입력 값입니다.")
    @Min(value = 0, message = "평점은 0 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5 이하여야 합니다.")
    private Float ratingAvg;
}
