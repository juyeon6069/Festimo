package com.example.festimo.post;

import com.example.festimo.domain.post.dto.DeletePostRequest;
import com.example.festimo.domain.post.dto.PostRequest;
import com.example.festimo.domain.post.entity.Post;
import com.example.festimo.domain.post.entity.PostCategory;
import com.example.festimo.domain.post.repository.PostRepository;
import com.example.festimo.domain.user.domain.User;
import com.example.festimo.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PostAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private Long savedPostId;

    @BeforeEach
    void setup() {
        postRepository.deleteAll();
        userRepository.deleteAll();

        // 사용자 추가
        User user = User.builder()
                .userName("user")
                .email("test@example.com")
                .password("1234")
                .nickname("testUser")
                .role(User.Role.USER)
                .build();
        userRepository.save(user);

        User admin = User.builder()
                .userName("admin")
                .email("admin@example.com")
                .password("adminpassword")
                .nickname("adminUser")
                .role(User.Role.ADMIN)
                .build();
        userRepository.save(admin);

        // 게시글 추가
        Post post = Post.builder()
                .title("Test Post")
                .content("Test Content")
                .password("1234")
                .user(user)
                .writer(user.getNickname())
                .mail(user.getEmail())
                .category(PostCategory.NOTICE)
                .build();
        Post savedPost = postRepository.save(post);

        // 저장된 게시글 ID 저장
        savedPostId = savedPost.getId();
    }

    @Test
    @DisplayName("비회원 게시글 등록 시 접근 불가")
    void testNonMemberCannotCreatePost() throws Exception {
        PostRequest request = new PostRequest();
        request.setTitle("Test Title");
        request.setContent("Test Content");
        request.setMail("abc@gmail.com");
        request.setWriter("testWriter");
        request.setCategory(PostCategory.NOTICE);
        request.setPassword("1234");

        mockMvc.perform(post("/api/companions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 게시글 등록 성공")
    void testMemberCanCreatePost() throws Exception {
        // Given
        PostRequest request = PostRequest.builder()
                .title("Test Title")
                .writer("Test Writer")
                .mail("test@example.com")
                .password("1234")
                .content("Test Content")
                .category(PostCategory.NOTICE)
                .build();

        // Set SecurityContext for authenticated user
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("test@example.com", null, "ROLE_USER")
        );

        // When & Then
        mockMvc.perform(post("/api/companions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("비회원 게시글 전체 조회 가능")
    void testNonMemberCanViewPosts() throws Exception {
        mockMvc.perform(get("/api/companions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 게시글 삭제 성공")
    void testMemberCanDeletePost() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("test@example.com", null, "ROLE_USER")
        );

        DeletePostRequest request = new DeletePostRequest();
        request.setPassword("1234");

        mockMvc.perform(delete("/api/companions/" + savedPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("관리자 게시글 삭제 성공")
    void testAdminCanDeletePost() throws Exception {
        // Set SecurityContext for authenticated admin
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin@example.com", null, "ROLE_ADMIN")
        );

        DeletePostRequest request = new DeletePostRequest();
        request.setPassword("adminpassword");

        mockMvc.perform(delete("/api/companions/" + savedPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}

