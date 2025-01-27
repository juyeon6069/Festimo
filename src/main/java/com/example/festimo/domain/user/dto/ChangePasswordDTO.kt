package com.example.festimo.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {
    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;
}