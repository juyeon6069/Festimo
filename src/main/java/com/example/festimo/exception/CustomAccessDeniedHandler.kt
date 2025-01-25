package com.example.festimo.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.apply {
            status = HttpServletResponse.SC_FORBIDDEN
            contentType = "application/json"
            characterEncoding = "UTF-8"
            writer.write("{\"error\": \"ACCESS_DENIED\", \"message\": \"권한이 없습니다\"}")
        }
    }
}