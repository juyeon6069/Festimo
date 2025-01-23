package com.example.festimo.admin;

import com.example.festimo.domain.user.domain.User;
import com.example.festimo.domain.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminUserDeleteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        createTestUser("admin@example.com", "adminpassword", User.Role.ADMIN, "관리자");
        createTestUser("user@example.com", "userpassword", User.Role.USER, "일반 사용자");
    }

    private void createTestUser(String email, String password, User.Role role, String userName) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .userName(userName)
                    .nickname(userName + "_nick")
                    .createdDate(LocalDateTime.now())
                    .build();
            userRepository.save(user);
        }
    }

    private String getAuthToken(String email, String password) throws Exception {
        String requestBody = """
                {
                "email": "%s",
                "password": "%s"
                }
                """.formatted(email, password);

        String response = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.parse(response).read("$.accessToken"); // AccessToken 추출
    }

    @Test
    @DisplayName("관리자가 회원 삭제")
    public void testAdminCanDeleteUser() throws Exception {
        // Given: 관리자 로그인 후 토큰 발급
        String adminToken = getAuthToken("admin@example.com", "adminpassword");

        // 사용자 ID 가져오기 (삭제 대상)
        User user = userRepository.findByEmail("user@example.com").orElseThrow();
        Long userId = user.getId();

        // When & Then: 관리자가 회원 삭제 API 호출
        mockMvc.perform(delete("/api/admin/users/{userId}", userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("일반 사용자가 회원 삭제 시도")
    public void testUserCannotDeleteUser() throws Exception {
        // Given: 일반 사용자 로그인 후 토큰 발급
        String userToken = getAuthToken("user@example.com", "userpassword");

        // 사용자 ID 가져오기 (삭제 대상)
        User user = userRepository.findByEmail("user@example.com").orElseThrow();
        Long userId = user.getId();

        // When & Then: 일반 사용자가 회원 삭제 API 호출
        mockMvc.perform(delete("/api/admin/users/{userId}", userId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("비인증 상태로 회원 삭제 시 실패")
    public void testUnauthorizedCannotDeleteUser() throws Exception {
        // Given: 사용자 ID 가져오기 (삭제 대상)
        User user = userRepository.findByEmail("user@example.com").orElseThrow();
        Long userId = user.getId();

        // When & Then: 인증 없이 회원 삭제 API 호출
        mockMvc.perform(delete("/api/admin/users/{userId}", userId))
                .andExpect(status().isUnauthorized());
    }
}
