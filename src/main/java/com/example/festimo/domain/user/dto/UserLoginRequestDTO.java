package com.example.festimo.domain.user.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserLoginRequestDTO {
    private String email;
    private String password;
}
