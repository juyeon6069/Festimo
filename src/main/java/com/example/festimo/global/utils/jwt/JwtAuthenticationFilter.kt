package com.example.festimo.global.utils.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    private val logger: Logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val requestPath: String = request.requestURI
        logger.debug("Incoming request path: {}", requestPath)

        // 인증 제외 경로 처리
        if (requestPath.startsWith("/swagger-ui") ||
            requestPath.startsWith("/v3/api-docs") ||
            requestPath.startsWith("/uploads")) {
            logger.debug("인증 제외 경로: {}", requestPath)
            chain.doFilter(request, response)
            return
        }

        val token = jwtTokenProvider.resolveToken(request)
        if (token != null && jwtTokenProvider.validateToken(token)) {
            logger.debug("Valid token found for path: {}", requestPath)
            SecurityContextHolder.getContext().authentication = jwtTokenProvider.getAuthentication(token)
        }

        chain.doFilter(request, response)
    }
}