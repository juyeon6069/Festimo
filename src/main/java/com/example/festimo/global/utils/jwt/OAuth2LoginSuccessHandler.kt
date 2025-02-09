package com.example.festimo.global.utils.jwt

import com.example.festimo.domain.oauth.dto.CustomOAuth2User
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class OAuth2LoginSuccessHandler(private val jwtTokenProvider: JwtTokenProvider) :
    SimpleUrlAuthenticationSuccessHandler() {
    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest, response: HttpServletResponse,
        authentication: Authentication
    ) {
        // 인증된 사용자 정보 가져오기

        val oauth2User = authentication.principal as CustomOAuth2User // 수정됨

        // 이메일 확인
        val email = oauth2User.email
        val provider = oauth2User.provider
        println(email)

        // 이메일이 정상적으로 제공된 경우 JWT 토큰 생성
        val role = oauth2User.authorities.iterator().next().authority // 역할 가져오기
        // 홈 화면으로 이동
        redirectStrategy.sendRedirect(
            request, response,
            "/html/oauth2redirect.html?email=$email"
        )
    }
}


