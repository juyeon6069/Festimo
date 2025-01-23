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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminUserListTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        createTestUser("admin@example.com", "adminpassword", User.Role.ADMIN, "관리자");
        createTestUser("user@example.com", "userpassword", User.Role.USER, "일반 사용자");
    }

    private void createTestUser(String email, String password, User.Role role, String userName) {
        if(!userRepository.existsByEmail(email)&&!userRepository.existsByNickname(userName+"_nick")) {
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

        return JsonPath.parse(response).read("$.accessToken"); //AccessToken 추출
    }


    @Test
    @DisplayName("관리자가 회원 조회")
    public void testAdminCanViewUsers() throws Exception {
        // Given: 관리자 로그인 후 토큰 발급
        String adminToken = getAuthToken("admin@example.com", "adminpassword");

        // When & Then: 발급받은 토큰으로 회원 조회 API 호출
        mockMvc.perform(get("/api/admin/users?page=0&size=5")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").exists())
                .andExpect(jsonPath("$.content[0].role").exists());
    }

    @Test
    @DisplayName("일반 사용자가 회원 조회 시 거부")
    public void testUserCannotViewUsers() throws Exception {
        //Given : 일반 사용자 로그인 후 토큰 발급
        String userToken = getAuthToken("user@example.com", "userpassword");

        //When & Then : 발급받은 토큰으로 회원 조회 API 호출
        mockMvc.perform(get("/api/admin/users?page=0&size=5")
                .header("Authorization", "Bearer "+userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("비인증 상태로 회원 조회 시 실패")
    public void testUnauthorizedCannotViewUsers() throws Exception {
        // When & Then: 인증 없이 API 호출
        mockMvc.perform(get("/api/admin/users?page=0&size=5"))
                .andExpect(status().isUnauthorized());
    }


}
