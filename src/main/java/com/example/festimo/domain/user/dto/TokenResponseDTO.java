package com.example.festimo.domain.user.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponseDTO {
    private String accessToken;
    private String refreshToken;
}
