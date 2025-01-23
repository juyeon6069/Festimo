package com.example.festimo.post;

import com.example.festimo.domain.post.entity.Comment;
import com.example.festimo.domain.post.entity.Post;
import com.example.festimo.domain.post.repository.CommentRepository;
import com.example.festimo.domain.post.repository.PostRepository;
import com.example.festimo.domain.post.service.PostServiceImpl;
import com.example.festimo.domain.user.domain.User;
import com.example.festimo.domain.user.repository.UserRepository;
import com.example.festimo.exception.CommentDeleteAuthorizationException;
import com.example.festimo.exception.CommentNotFound;
import com.example.festimo.exception.PostNotFound;
import com.example.festimo.exception.UnauthorizedException;
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
public class UpdateDeleteCommentTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostServiceImpl postService;

    private Post savedPost;
    private Comment savedComment;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

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
                .mail("example@example.com")
                .password("1234")
                .content("테스트 내용")
                .build();
        postRepository.save(savedPost);

        // 댓글 저장
        savedComment = Comment.builder()
                .post(savedPost)
                .nickname("nickname")
                .comment("테스트 댓글")
                .sequence(1)
                .build();
        commentRepository.save(savedComment);

        userRepository.save(User.builder()
                .userName("user2")
                .email("other@example.com")
                .nickname("otherUser")
                .role(User.Role.USER)
                .build());

        // 인증 객체 생성
        authentication = new TestingAuthenticationToken(user.getEmail(), null, "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void testDeleteComment_Success() {
        // When
        postService.deleteComment(savedPost.getId(), savedComment.getSequence(), authentication);

        // Then
        assertFalse(commentRepository.findById(savedComment.getId()).isPresent());
    }

    @Test
    @DisplayName("본인이 아닌 사용자에 의한 댓글 삭제 실패")
    void testDeleteComment_Unauthorized() {
        // Given: 다른 사용자를 인증 객체로 설정
        Authentication otherAuthentication = new TestingAuthenticationToken("other@example.com", null, "ROLE_USER");

        // When & Then
        assertThrows(CommentDeleteAuthorizationException.class, () -> {
            postService.deleteComment(savedPost.getId(), savedComment.getSequence(), otherAuthentication);
        });
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 실패")
    void testDeleteComment_CommentNotFound() {
        // Given: 잘못된 댓글 시퀀스
        int invalidSequence = 999;

        // When & Then
        assertThrows(CommentNotFound.class, () -> {
            postService.deleteComment(savedPost.getId(), invalidSequence, authentication);
        });
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 댓글 삭제 실패")
    void testDeleteComment_PostNotFound() {
        // Given: 잘못된 게시글 ID
        Long invalidPostId = -1L;

        // When & Then
        assertThrows(PostNotFound.class, () -> {
            postService.deleteComment(invalidPostId, savedComment.getSequence(), authentication);
        });
    }

    @Test
    @DisplayName("비로그인 사용자의 댓글 삭제 실패")
    void testDeleteComment_Unauthorized2() {
        // Given: 인증되지 않은 사용자 (null authentication)
        Authentication unauthenticated = new TestingAuthenticationToken(null, null);

        // When & Then
        assertThrows(UnauthorizedException.class, () -> {
            postService.deleteComment(savedPost.getId(), savedComment.getSequence(), unauthenticated);
        });
    }
}
