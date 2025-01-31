package com.example.festimo.global.utils.jwt

import com.example.festimo.exception.CustomException
import com.example.festimo.exception.ErrorCode
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*


@Component
class JwtTokenProvider(
    @Value("\${spring.jwt.secret}") secretKey: String,
    @Value("\${spring.jwt.access-expiration}") private val accessTokenExpiration: Long,
    @Value("\${spring.jwt.refresh-expiration}") private val refreshTokenExpiration: Long
) {
    private val key: Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))

    // Access Token 생성
    fun generateAccessToken(email: String, role: String): String {
        return Jwts.builder()
            .setSubject(email)
            .claim("role", role)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + accessTokenExpiration))
            .signWith(key)
            .compact()
    }

    // Refresh Token 생성
    fun generateRefreshToken(email: String): String {
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + refreshTokenExpiration))
            .signWith(key)
            .compact()
    }

    // 토큰에서 이메일 추출
    fun getEmailFromToken(token: String): String {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
                .subject
        } catch (e: ExpiredJwtException) {
            throw CustomException(ErrorCode.TOKEN_EXPIRED)
        } catch (e: JwtException) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        } catch (e: IllegalArgumentException) {
            throw CustomException(ErrorCode.INVALID_TOKEN)
        }
    }

    // 토큰 검증
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
            true
        } catch (e: ExpiredJwtException) {
            println("Token expired: ${e.message}")
            false
        } catch (e: JwtException) {
            println("Invalid token: ${e.message}")
            false
        } catch (e: IllegalArgumentException) {
            println("Invalid token: ${e.message}")
            false
        }
    }

    // 인증 객체 생성
    fun getAuthentication(token: String): Authentication {
        val email = getEmailFromToken(token)
        val role = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body["role"] as String

        val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
        return UsernamePasswordAuthenticationToken(email, null, authorities)
    }

    // 요청에서 토큰 추출
    fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (!bearerToken.isNullOrEmpty() && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}