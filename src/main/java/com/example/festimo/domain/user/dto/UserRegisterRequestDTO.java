package com.example.festimo.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequestDTO {
    private String userName;
    private String nickname;
    private String email;
    private String password;
    private String gender;

}