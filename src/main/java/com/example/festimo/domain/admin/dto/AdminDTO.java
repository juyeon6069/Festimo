package com.example.festimo.domain.admin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AdminDTO {
    private Long userId;
    private String userName;
    private String nickname;
    private String email;
    private String role; // ADMIN, USER
    private LocalDateTime createdDate;
    private String gender; // F,M
    private Float ratingAvg;

}
