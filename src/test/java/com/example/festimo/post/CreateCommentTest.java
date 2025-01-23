package com.example.festimo.post;

import com.example.festimo.domain.post.dto.CommentRequest;
import com.example.festimo.domain.post.dto.CommentResponse;
import com.example.festimo.domain.post.entity.Post;
import com.example.festimo.domain.post.repository.PostRepository;
import com.example.festimo.domain.post.service.PostServiceImpl;
import com.example.festimo.domain.user.domain.User;
import com.example.festimo.domain.user.repository.UserRepository;
import com.example.festimo.exception.PostNotFound;
import com.example.festimo.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CreateCommentTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostServiceImpl postService;

    private Post savedPost;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성 및 저장
        User user = User.builder()
                .userName("user")
                .email("example@example.com")
                .nickname("nickname")
                .password("password1234")
                .role(User.Role.USER)
                .build();
        userRepository.save(user);

        // 게시글 저장
        savedPost = Post.builder()
                .title("테스트 제목")
                .writer("테스트 작성자")
                .mail(user.getEmail())
                .password("1234")
                .content("테스트 내용")
                .build();
        postRepository.save(savedPost);

        // SecurityContext에 인증 정보 설정
        authentication = new TestingAuthenticationToken(user.getEmail(), null, "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("댓글 등록 성공")
    void testCreateComment_Success() {
        // Given
        CommentRequest request = new CommentRequest("nickname", "테스트 내용");

        // When
        CommentResponse response = postService.createComment(savedPost.getId(), request, SecurityContextHolder.getContext().getAuthentication());

        // Then
        assertNotNull(response.getId());
        assertEquals("nickname", response.getNickname());
        assertEquals("테스트 내용", response.getComment());
        assertEquals(savedPost.getId(), response.getPostId());
    }

    @Test
    @DisplayName("댓글 등록 시 필수 필드 누락")
    void testCreateComment_MissingFields() {
        // Given
        CommentRequest request = new CommentRequest(null, "테스트 내용");

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            postService.createComment(savedPost.getId(), request, authentication);
        });
    }

    @Test
    @DisplayName("인증되지 않은 사용자로 댓글 작성 실패")
    void testCreateComment_Unauthorized() {
        // Given: 인증되지 않은 Authentication 객체
        Authentication invalidAuth = null;
        CommentRequest request = new CommentRequest("nickname", "테스트 내용");

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            postService.createComment(savedPost.getId(), request, invalidAuth);
        });
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 댓글 작성")
    void testCreateComment_PostNotFound() {
        // Given
        Long invalidPostId = -1L;
        CommentRequest request = new CommentRequest("nickname", "테스트 내용");

        // When & Then
        assertThrows(PostNotFound.class, () -> {
            postService.createComment(invalidPostId, request, authentication);
        });
    }
}

