package com.example.festimo.domain.oauth.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

	private final Map<String, Object> attributes;
	private final String provider;
	private final String email;
	private final String nickname;
	private final String role;

	public CustomOAuth2User(Map<String, Object> attributes, String provider, String email, String nickname,
		String role) { // 수정됨
		this.attributes = attributes;
		this.provider = provider;
		this.email = email;
		this.nickname = nickname;
		this.role = role;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> collection = new ArrayList<>();
		collection.add(() -> role); // 수정됨: 역할 반환
		return collection;
	}

	@Override
	public String getName() {
		return nickname; // 수정됨: 닉네임 반환
	}

	public String getEmail() {
		return email; // 수정됨
	}

	public String getProvider() {
		return provider; // 수정됨
	}
}
