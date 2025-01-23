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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminUserUpdateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        createTestUser("admin@example.com", "adminpassword", User.Role.ADMIN, "관리자");
        createTestUser("user@example.com", "userpassword", User.Role.USER, "일반 사용자");
    }

    private void createTestUser(String email, String password, User.Role role, String userName) {
        if (!userRepository.existsByEmail(email) && !userRepository.existsByNickname(userName + "_nick")) {
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
    @DisplayName("회원 정보 수정 성공")
    public void testUpdateUser_Success() throws Exception {
        // Given: 관리자 토큰 발급 및 수정 대상 사용자 ID
        String adminToken = getAuthToken("admin@example.com", "adminpassword");
        Long userId = userRepository.findByEmail("user@example.com").orElseThrow().getId();

        String updateRequestBody = """
            {
                "userName": "Updated User",
                "nickname": "UpdatedNick",
                "email": "updateduser@example.com",
                "gender": "M",
                "ratingAvg": 4.5
            }
        """;

        // When & Then: 회원 수정 API 호출
        mockMvc.perform(put("/api/admin/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Updated User"))
                .andExpect(jsonPath("$.nickname").value("UpdatedNick"))
                .andExpect(jsonPath("$.email").value("updateduser@example.com"));
    }

    @Test
    @DisplayName("회원 정보 수정 실패 : 존재하지 않는 사용자 ID")
    public void testUpdateUser_NotFound() throws Exception {
        // Given: 관리자 토큰 발급
        String adminToken = getAuthToken("admin@example.com", "adminpassword");

        String updateRequestBody = """
            {
                "userName": "Updated User",
                "nickname": "UpdatedNick",
                "email": "updateduser@example.com",
                "gender": "M",
                "ratingAvg": 4.5
            }
        """;

        // When & Then: 잘못된 사용자 ID로 API 호출
        mockMvc.perform(put("/api/admin/users/9999999") // 존재하지 않는 ID
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("회원 정보 수정 실패 : 이름 누락")
    public void testUpdateUser_Fail_MissingUserName() throws Exception {
        // Given: 관리자 토큰 발급 및 수정 대상 사용자 ID
        String adminToken = getAuthToken("admin@example.com", "adminpassword");
        Long userId = userRepository.findByEmail("user@example.com").orElseThrow().getId();

        String updateRequestBody = """
        {
            "userName": "",
            "nickname": "UpdatedNick",
            "email": "updateduser@example.com",
            "gender": "M",
            "ratingAvg": 4.5
        }
    """;

        // When & Then: 이름 누락 시 유효성 검사 실패
        mockMvc.perform(put("/api/admin/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원 정보 수정 실패 : 성별 값 오류")
    public void testUpdateUser_Fail_InvalidGender() throws Exception {
        // Given: 관리자 토큰 발급 및 수정 대상 사용자 ID
        String adminToken = getAuthToken("admin@example.com", "adminpassword");
        Long userId = userRepository.findByEmail("user@example.com").orElseThrow().getId();

        String updateRequestBody = """
        {
            "userName": "Updated User",
            "nickname": "UpdatedNick",
            "email": "updateduser@example.com",
            "gender": "Z",
            "ratingAvg": 4.5
        }
    """;

        // When & Then: 성별 값이 잘못된 경우
        mockMvc.perform(put("/api/admin/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("회원 정보 수정 실패 : 평점 범위 초과")
    public void testUpdateUser_Fail_InvalidRating() throws Exception {
        // Given: 관리자 토큰 발급 및 수정 대상 사용자 ID
        String adminToken = getAuthToken("admin@example.com", "adminpassword");
        Long userId = userRepository.findByEmail("user@example.com").orElseThrow().getId();

        String updateRequestBody = """
        {
            "userName": "Updated User",
            "nickname": "UpdatedNick",
            "email": "updateduser@example.com",
            "gender": "M",
            "ratingAvg": 6.7
        }
    """;

        // When & Then: 평점 값이 5 초과인 경우
        mockMvc.perform(put("/api/admin/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody))
                .andExpect(status().isBadRequest());
    }

}

