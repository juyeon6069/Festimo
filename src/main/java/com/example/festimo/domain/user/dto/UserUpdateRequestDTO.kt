package com.example.festimo.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDTO {
	private String nickname; // 닉네임
	private String userName; // 사용자 이름
	private String gender; // 성별
}
