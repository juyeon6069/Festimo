/*package com.example.festimo.meet;

import com.example.festimo.domain.meet.entity.Companion;
import com.example.festimo.domain.meet.repository.CompanionMemberRepository;
import com.example.festimo.domain.meet.repository.CompanionRepository;
import com.example.festimo.domain.post.entity.Post;
import com.example.festimo.domain.post.repository.PostRepository;
import com.example.festimo.domain.user.domain.User;
import com.example.festimo.domain.user.repository.UserRepository;
import com.example.festimo.global.utils.jwt.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CreateApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CompanionRepository companionRepository;

    @Autowired
    private CompanionMemberRepository companionMemberRepository;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성 및 저장
        testUser = userRepository.save(
                User.builder()
                        .userName("testUser")
                        .email("test@example.com")
                        .nickname("nickname")
                        .password("password1234")
                        .role(User.Role.USER)
                        .build()
        );

        // 테스트용 게시글 생성 및 저장
        testPost = postRepository.save(
                Post.builder()
                        .title("Test Post Title")
                        .writer("testUser")
                        .mail(testUser.getEmail())
                        .password("1234")
                        .content("Test Post Content")
                        .build()
        );
    }

    @Test
    @DisplayName("동행 생성 성공")
    void testCreateCompanion_Success() throws Exception {
        // JWT 생성
        String token = jwtTokenProvider.generateAccessToken(testUser.getEmail(), "USER");

        // 요청 본문 생성
        String requestBody = "{\"postId\": " + testPost.getId() + "}";

        // API 호출
        mockMvc.perform(post("/api/meet/companions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        // DB 확인: Companion 생성 확인
        Companion companion = companionRepository.findByPost(testPost)
                .orElseThrow(() -> new AssertionError("Companion not found in database"));
        assertNotNull(companion);
        assertEquals(testPost.getId(), companion.getPost().getId());
        assertEquals(testUser.getId(), companion.getLeaderId());

        // DB 확인: CompanionMember에 리더 추가 확인
        boolean isLeaderAdded = companionMemberRepository.existsByCompanionIdAndUserId(companion.getCompanionId(), testUser.getId());
        assertTrue(isLeaderAdded, "Leader not added to CompanionMember");
    }

    @Test
    @DisplayName("중복된 동행 생성 시 예외 발생")
    void testCreateCompanion_DuplicateCompanion() throws Exception {
        // 동행 생성
        Companion companion = companionRepository.save(
                new Companion(null, testUser.getId(), LocalDateTime.now(), testPost)
        );

        // JWT 생성
        String token = jwtTokenProvider.generateAccessToken(testUser.getEmail(), "USER");

        // 요청 본문 생성
        String requestBody = "{\"postId\": " + testPost.getId() + "}";

        // API 호출 및 예외 확인
        mockMvc.perform(post("/api/meet/companions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict()); // COMPANION_ALREADY_EXISTS 예외 처리
    }
}
*/