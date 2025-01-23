package com.example.festimo.meet;
/*
import com.example.festimo.domain.meet.dto.ApplicationRequest;
import com.example.festimo.domain.meet.dto.CompanionRequest;
import com.example.festimo.domain.meet.dto.LeaderApplicationResponse;
import com.example.festimo.domain.user.dto.UserLoginRequestDTO;
import com.example.festimo.domain.user.dto.UserRegisterRequestDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 테스트 순서 지정
@Transactional
class TestScenario {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String user1Token;
    private static String user2Token;
    private static String user3Token;
    private static Long companionId;

    @Test
    @Order(1)
    void createUsers() throws Exception {
        // User1 생성 및 로그인
        user1Token = getOrCreateUser("user1@example.com", "!q1234567", "User1", "Nickname1", "M");

        // User2 생성 및 로그인
        user2Token = getOrCreateUser("user2@example.com", "!q1234567", "User2", "Nickname2", "F");

        // User3 생성 및 로그인
        user3Token = getOrCreateUser("user3@example.com", "!q1234567", "User3", "Nickname3", "M");
    }

    @Test
    @Order(2)
    void createCompanion() throws Exception {
        // User1이 Post 21로 동행 생성
        createCompanion(user1Token, 21L);

        // 중복된 동행 요청 -> 에러 없이 요청을 무시해야 함
        try {
            createCompanion(user1Token, 21L);
            System.out.println("중복된 동행 요청이 정상적으로 무시되었습니다.");
        } catch (Exception e) {
            fail("중복된 동행 요청이 에러를 발생시켰습니다: " + e.getMessage());
        }

        // 동행 생성이 성공적으로 진행되었는지 확인
        System.out.println("동행 생성 및 중복 요청 처리가 성공적으로 완료되었습니다.");
    }



    // 유틸리티 메서드

    //회원 생성
    private String getOrCreateUser(String email, String password, String userName, String nickname, String gender) throws Exception {
        try {
            // 1. 회원가입
            registerUser(userName, nickname, email, password, gender);
        } catch (Exception e) {
            // 2. 이메일 중복 예외 처리
            if (e.getMessage().contains("이메일 중복")) {
                return loginAndGetAccessToken(email, password);
            }
            throw e; // 다른 예외는 그대로 던짐
        }
        // 3. 회원가입 성공 시 로그인
        return loginAndGetAccessToken(email, password);
    }


    //회원가입
    private void registerUser(String userName, String nickname, String email, String password, String gender) throws Exception {
        UserRegisterRequestDTO request = new UserRegisterRequestDTO();
        request.setUserName(userName);
        request.setNickname(nickname);
        request.setEmail(email);
        request.setPassword(password);
        request.setGender(gender);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    //로그인
    private String loginAndGetAccessToken(String email, String password) throws Exception {
        UserLoginRequestDTO request = new UserLoginRequestDTO();
        request.setEmail(email);
        request.setPassword(password);

        String response = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, String> responseBody = objectMapper.readValue(response, new TypeReference<>() {});
        return responseBody.get("accessToken");
    }

    //동행 생성
    private void createCompanion(String token, Long postId) throws Exception {
        CompanionRequest request = new CompanionRequest();
        request.setPostId(postId);

        try {
            // 동행 생성 요청
            String response = mockMvc.perform(post("/api/meet/companions")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated()) // 201 상태 코드 확인
                    .andReturn().getResponse().getContentAsString();


        } catch (Exception e) {
            // 중복 예외 발생 시 단순히 넘어감
            if (e.getMessage().contains("COMPANION_ALREADY_EXISTS")) {
                System.out.println("동행이 이미 존재합니다. 아무 작업 없이 넘어갑니다.");
            }
            else {
                // 다른 예외는 그대로 던짐
                throw e;
            }
        }
    }

}

*/