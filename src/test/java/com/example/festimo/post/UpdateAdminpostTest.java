package com.example.festimo.post;

import com.example.festimo.domain.post.dto.PostDetailResponse;
import com.example.festimo.domain.post.dto.UpdatePostRequest;
import com.example.festimo.domain.post.entity.Post;
import com.example.festimo.domain.post.entity.PostCategory;
import com.example.festimo.domain.post.repository.PostRepository;
import com.example.festimo.domain.post.service.PostServiceImpl;
import com.example.festimo.exception.InvalidPasswordException;
import com.example.festimo.exception.PostNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UpdateAdminpostTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostServiceImpl postService;

    private Post existingPost;

    @BeforeEach
    void setUp() {
        existingPost = Post.builder()
                .title("Original Title")
                .content("Original Content")
                .category(PostCategory.NOTICE)
                .writer("Writer")
                .mail("example@mail.com")
                .password("password123")
                .build();
        postRepository.saveAndFlush(existingPost);
    }

    @Test
    @DisplayName("수정 요청에서 null 값이 들어오면 기존 필드가 유지된다")
    void testUpdatePost_NullFieldsRemainUnchanged() {
        // Given
        UpdatePostRequest request = UpdatePostRequest.builder()
                .title(null) // 제목은 수정하지 않음
                .content("새로운 내용") // 내용만 수정
                .password("password123")
                .build();

        // When
        PostDetailResponse response = postService.updatePost(existingPost.getId(), request);

        // Then
        assertEquals("Original Title", response.getTitle(), "제목은 기존 값 유지");
        assertEquals("새로운 내용", response.getContent(), "내용은 업데이트됨");
        assertEquals(PostCategory.NOTICE, response.getCategory(), "카테고리는 기존 값 유지");
    }

    @Test
    @DisplayName("수정 요청에서 일부 필드만 수정된다")
    void testUpdatePost_PartialUpdate() {
        // Given
        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("Updated Title") // 제목 수정
                .content(null)         // 내용은 유지
                .category(null)        // 카테고리는 유지
                .password("password123") // 비밀번호
                .build();

        // When
        PostDetailResponse response = postService.updatePost(existingPost.getId(), request);

        // Then
        assertEquals("Updated Title", response.getTitle(), "제목은 업데이트됨");
        assertEquals("Original Content", response.getContent(), "내용은 기존 값 유지");
        assertEquals(PostCategory.NOTICE, response.getCategory(), "카테고리는 기존 값 유지");
    }

    @Test
    @DisplayName("수정 요청 시 잘못된 비밀번호로 수정 실패")
    void testUpdatePost_InvalidPassword() {
        // Given
        UpdatePostRequest request = new UpdatePostRequest(
                "Updated Title",
                "Updated Content",
                PostCategory.REVIEW,
                "wrongPassword"
        );

        // When & Then
        assertThrows(InvalidPasswordException.class, () -> {
            postService.updatePost(existingPost.getId(), request);
        }, "InvalidPasswordException should be thrown for incorrect password");
    }

    @Test
    @DisplayName("수정 시 updatedAt이 변경된다")
    void testUpdatePost_UpdatedAtChanges() throws InterruptedException {
        // Given
        LocalDateTime beforeUpdate = existingPost.getUpdatedAt();

        Thread.sleep(1000);
        UpdatePostRequest request = new UpdatePostRequest("New Title", "New Content", PostCategory.QNA, "password123");

        // When
        postService.updatePost(existingPost.getId(), request);
        postRepository.flush();
        Post updatedPost = postRepository.findById(existingPost.getId()).orElseThrow();

        // Then
        assertNotNull(updatedPost.getUpdatedAt(), "updatedAt은 null이 아니어야 합니다.");
        assertNotEquals(beforeUpdate, updatedPost.getUpdatedAt(), "updatedAt이 업데이트되어야 합니다.");
        assertEquals("New Title", updatedPost.getTitle(), "제목이 업데이트되어야 합니다.");
        assertEquals("New Content", updatedPost.getContent(), "내용이 업데이트되어야 합니다.");
        assertEquals(PostCategory.QNA, updatedPost.getCategory(), "카테고리가 업데이트되어야 합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 PostNotFound 예외 발생")
    void testUpdatePost_NotFound() {
        // Given
        Long invalidPostId = 999L;
        UpdatePostRequest request = new UpdatePostRequest(
                "Updated Title",
                "Updated Content",
                null,
                "password123"
        );

        // When & Then
        assertThrows(PostNotFound.class, () -> {
            postService.updatePost(invalidPostId, request);
        }, "PostNotFound 예외가 발생해야 한다");
    }
}

