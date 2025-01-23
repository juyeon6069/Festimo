package com.example.festimo.domain.oauth.dto;

import java.util.Map;

public class KakaoOAuth2Response {

	private final Map<String, Object> attributes;
	private final Map<String, Object> kakaoAccount;
	private final Map<String, Object> properties;

	public KakaoOAuth2Response(Map<String, Object> attributes) {
		this.attributes = attributes;
		this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
		this.properties = (Map<String, Object>) attributes.get("properties");
	}

	public String getProvider() {
		return "kakao";
	}

	public String getProviderId() {
		return attributes.get("id").toString();
	}

	public String getEmail() {
		if (kakaoAccount != null && kakaoAccount.get("email") != null) {
			return kakaoAccount.get("email").toString();
		}
		return null;
	}

	public String getNickname() {
		if (properties != null && properties.get("nickname") != null) {
			return properties.get("nickname").toString();
		}
		return "Unknown";
	}


}
