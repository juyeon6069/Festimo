package com.example.festimo.domain.user.controller

import com.example.festimo.domain.user.dto.*
import com.example.festimo.domain.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.Duration


@Tag(name = "회원 관리 API", description = "회원 정보 관리하는 API")
@RestController
@RequestMapping("/api")
//@RequiredArgsConstructor
class UserController (
    private val userService: UserService
){

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    fun register(@RequestBody dto: @Valid UserRegisterRequestDTO): ResponseEntity<String> {
        return ResponseEntity.ok(userService.register(dto))
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    fun login(@RequestBody dto: @Valid UserLoginRequestDTO): ResponseEntity<*> {
        println("login controller")
        val tokenResponseDTO = userService.login(dto)
        val accessToken = tokenResponseDTO.accessToken
        val refreshToken = tokenResponseDTO.refreshToken

        val refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(Duration.ofDays(30))
            .sameSite("Strict").build()

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
            .body(
                java.util.Map.of(
                    "accessToken", accessToken,
                    "nickname", tokenResponseDTO.nickname,
                    "email", tokenResponseDTO.email,
                    "id", tokenResponseDTO.id,
                )
            )
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<String> {
        val cookies = request.cookies
        var refreshToken: String? = null
        if (cookies != null) {
            for (cookie in cookies) {
                if (cookie.name == "refreshToken") {
                    refreshToken = "Bearer " + cookie.value
                    println(refreshToken)
                    break
                }
            }
        }
        require(!(refreshToken == null || !refreshToken.startsWith("Bearer "))) { "Invalid token format." }
        userService.logout(refreshToken.substring(7)) // 앞에 접두사 Bearer 제외
        val cookie = Cookie("refreshToken", null)
        cookie.isHttpOnly = true
        cookie.secure = true // HTTPS 환경에서만 쿠키가 전송되도록 설정
        cookie.path = "/" // 전체 도메인에서 쿠키 유효
        cookie.maxAge = 0 // 쿠키 만료 시간을 0으로 설정하여 삭제
        response.addCookie(cookie)

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshToken).body("Logout successful.")
    }

    @Operation(summary = "회원 정보 조회")
    @GetMapping("/user/{email}")
    fun getUser(@PathVariable email: String): ResponseEntity<UserResponseDTO> {
        return ResponseEntity.ok(userService.getUserByEmail(email))
    }

    @Operation(summary = "회원 마이페이지 정보 조회")
    @GetMapping("/user/mypage")
    fun getUser(authentication: Authentication): ResponseEntity<UserResponseDTO> {
        val email = authentication.name // 인증된 사용자의 이메일 가져오기
        return ResponseEntity.ok(userService.getUserByEmail(email))
    }

    @Operation(summary = "비밀번호 변경")
    @PostMapping("/user/mypage/change-password")
    fun changePassword(
        authentication: Authentication,
        @RequestBody dto: @Valid ChangePasswordDTO
    ): ResponseEntity<String> {
        // 인증된 사용자의 이메일 추출
        val email = authentication.name

        // 서비스 로직 호출
        return ResponseEntity.ok(userService.changePassword(email, dto))
    }


    @Operation(summary = "회원 정보 갱신")
    @PutMapping("/user/mypage/update")
    fun updateUser(authentication: Authentication, @RequestBody dto: UserUpdateRequestDTO): ResponseEntity<String> {
        val email = authentication.name // 인증된 사용자의 이메일 추출
        return ResponseEntity.ok(userService.updateUser(email, dto))
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/user/mypage/delete")
    fun deleteUser(authentication: Authentication): ResponseEntity<String> {
        val email = authentication.name // 인증된 사용자의 이메일 추출
        userService.deleteUser(email) // 계정 삭제

        // Refresh Token 쿠키 무효화
        val invalidRefreshTokenCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0) // 쿠키 만료
            .sameSite("Strict")
            .build()

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, invalidRefreshTokenCookie.toString()) // 쿠키 삭제
            .body("회원 탈퇴가 완료되었습니다.")
        // return ResponseEntity.ok(userService.deleteUser(email));
    }


    //    //현재 인증된 유저 정보 반환
    //    @GetMapping("/user")
    //    public ResponseEntity<UserResponseDTO> getAuthenticatedUser(@RequestHeader("Authorization") String accessToken) {
    //        String email = jwtTokenProvider.getEmailFromToken(accessToken.replace("Bearer ", ""));
    //        return ResponseEntity.ok(userService.getUserByEmail(email));
    //    }
    @PostMapping("/refresh")
    fun refreshTokens(@RequestBody request: Map<String?, String?>): ResponseEntity<TokenResponseDTO> {
        val refreshToken = request["refreshToken"]
        return ResponseEntity.ok(refreshToken?.let { userService.refreshTokens(it) })
    }

    @Operation(summary = "현재 로그인한 사용자의 ID 반환")
    @GetMapping("/user/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<Map<String, Long>> {
        val email = authentication.name // Spring Security에서 인증된 사용자 이메일 가져오기
        val userId = userService.getUserIdByEmail(email) // 이메일을 기반으로 사용자 ID 조회
        return ResponseEntity.ok(mapOf("id" to userId))
    }
}
