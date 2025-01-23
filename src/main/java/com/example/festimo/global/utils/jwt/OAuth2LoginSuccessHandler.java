package com.example.festimo.global.utils.jwt;

import com.example.festimo.domain.oauth.dto.CustomOAuth2User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final JwtTokenProvider jwtTokenProvider;

	public OAuth2LoginSuccessHandler(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		// 인증된 사용자 정보 가져오기
		CustomOAuth2User oauth2User = (CustomOAuth2User)authentication.getPrincipal(); // 수정됨

		// 이메일 확인
		String email = oauth2User.getEmail();
		String provider = oauth2User.getProvider();
		System.out.println(email);

		// 이메일이 정상적으로 제공된 경우 JWT 토큰 생성
		String role = oauth2User.getAuthorities().iterator().next().getAuthority(); // 역할 가져오기
		// 홈 화면으로 이동
		getRedirectStrategy().sendRedirect(request, response
				, "/html/oauth2redirect.html?email=" + email);

	}
}


