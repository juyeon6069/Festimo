package com.example.festimo.domain.user.controller;


import com.example.festimo.domain.user.dto.*;
import com.example.festimo.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@Tag(name = "회원 관리 API", description = "회원 정보 관리하는 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRegisterRequestDTO dto) {
        return ResponseEntity.ok(userService.register(dto));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginRequestDTO dto) {
        TokenResponseDTO tokenResponseDTO = userService.login(dto);

        String accessToken = tokenResponseDTO.getAccessToken();
        String refreshToken = tokenResponseDTO.getRefreshToken();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Strict").build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(Map.of(
                        "accessToken", accessToken,
                        "nickname", tokenResponseDTO.getNickname(),
                        "email", tokenResponseDTO.getEmail()
                ));
    }

    // @Operation(summary = "로그아웃")
    // @PostMapping("/logout")
    // public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String refreshToken) {
    //     if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {  // Authorization 헤더 없을 경우, Bearer 토큰 형식 아닐경우
    //         throw new IllegalArgumentException("Invalid token format.");
    //     }
    //     userService.logout(refreshToken.substring(7));  // 앞에 접두사 Bearer 제외
    //     return ResponseEntity.ok("Logged out successfully.");
    //
    //     // // Refresh Token 쿠키 무효화
    //     // ResponseCookie invalidRefreshTokenCookie = ResponseCookie.from("refreshToken", "")
    //     //     .httpOnly(true)
    //     //     .secure(true)
    //     //     .path("/")
    //     //     .maxAge(0) // 쿠키 만료
    //     //     .sameSite("Strict")
    //     //     .build();
    //     //
    //     // return ResponseEntity.ok()
    //     //     .header(HttpHeaders.SET_COOKIE, invalidRefreshTokenCookie.toString()) // 쿠키 삭제
    //     //     .body("로그아웃이 완료되었습니다.");
    //     //
    // }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies= request.getCookies();
        String refreshToken= null;
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals("refreshToken")) {
                    refreshToken ="Bearer " + cookie.getValue();
                    System.out.println(refreshToken);
                    break;
                }
            }
        }
        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {  // Authorization 헤더 없을 경우, Bearer 토큰 형식 아닐경우
            throw new IllegalArgumentException("Invalid token format.");
        }
        userService.logout(refreshToken.substring(7));// 앞에 접두사 Bearer 제외
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);  // HTTPS 환경에서만 쿠키가 전송되도록 설정
        cookie.setPath("/");  // 전체 도메인에서 쿠키 유효
        cookie.setMaxAge(0);  // 쿠키 만료 시간을 0으로 설정하여 삭제
        response.addCookie(cookie);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshToken).body("Logout successful.");
    }

    @Operation(summary = "회원 정보 조회")
    @GetMapping("/user/{email}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Operation(summary = "회원 마이페이지 정보 조회")
    @GetMapping("/user/mypage")
    public ResponseEntity<UserResponseDTO> getUser(Authentication authentication) {
        String email = authentication.getName(); // 인증된 사용자의 이메일 가져오기
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Operation(summary = "비밀번호 변경")
    @PostMapping("/user/mypage/change-password")
    public ResponseEntity<String> changePassword(Authentication authentication, @RequestBody @Valid ChangePasswordDTO dto) {
        // 인증된 사용자의 이메일 추출
        String email = authentication.getName();

        // 서비스 로직 호출
        return ResponseEntity.ok(userService.changePassword(email, dto));
    }



    @Operation(summary = "회원 정보 갱신")
    @PutMapping("/user/mypage/update")
    public ResponseEntity<String> updateUser(Authentication authentication, @RequestBody UserUpdateRequestDTO dto) {
        String email = authentication.getName(); // 인증된 사용자의 이메일 추출
        return ResponseEntity.ok(userService.updateUser(email, dto));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/user/mypage/delete")
    public ResponseEntity<String> deleteUser(Authentication authentication) {
        String email = authentication.getName(); // 인증된 사용자의 이메일 추출
        userService.deleteUser(email); // 계정 삭제

        // Refresh Token 쿠키 무효화
        ResponseCookie invalidRefreshTokenCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0) // 쿠키 만료
            .sameSite("Strict")
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, invalidRefreshTokenCookie.toString()) // 쿠키 삭제
            .body("회원 탈퇴가 완료되었습니다.");
        // return ResponseEntity.ok(userService.deleteUser(email));
    }


    //    //현재 인증된 유저 정보 반환
//    @GetMapping("/user")
//    public ResponseEntity<UserResponseDTO> getAuthenticatedUser(@RequestHeader("Authorization") String accessToken) {
//        String email = jwtTokenProvider.getEmailFromToken(accessToken.replace("Bearer ", ""));
//        return ResponseEntity.ok(userService.getUserByEmail(email));
//    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refreshTokens(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        return ResponseEntity.ok(userService.refreshTokens(refreshToken));
    }


}
