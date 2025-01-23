package com.example.festimo.post;

import com.example.festimo.domain.post.dto.PostRequest;
import com.example.festimo.domain.post.dto.PostListResponse;
import com.example.festimo.domain.post.entity.PostCategory;
import com.example.festimo.domain.post.repository.PostRepository;
import com.example.festimo.domain.post.service.PostService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
public class CreateAdminpostTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("게시글 등록")
    @Rollback(value = false)
    void posting() {
        // Given
        PostRequest postRequest = new PostRequest();
        postRequest.setTitle("테스트 게시글 제목");
        postRequest.setWriter("작성자1");
        postRequest.setMail("test@example.com");
        postRequest.setPassword("1234");
        postRequest.setContent("테스트 내용입니다.");
        postRequest.setCategory(PostCategory.COMPANION);

        // When
        PostListResponse responseDto = postService.createPost(postRequest);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getTitle()).isEqualTo(postRequest.getTitle());
        assertThat(responseDto.getWriter()).isEqualTo(postRequest.getWriter());

        System.out.println("등록된 게시글 ID: " + responseDto.getId());
    }

    @Autowired
    private Validator validator;

    @Test
    @DisplayName("필수 입력값 누락으로 게시글 등록 실패")
    void failToPostWhenMissingRequiredFields() {
        // Given
        PostRequest postRequest = new PostRequest();
        postRequest.setTitle(null); // 제목 누락
        postRequest.setWriter("작성자1");
        postRequest.setMail("test@example.com");
        postRequest.setPassword("1234");
        postRequest.setContent("테스트 내용입니다.");
        postRequest.setCategory(PostCategory.QNA);

        // When
        Set<ConstraintViolation<PostRequest>> violations = validator.validate(postRequest);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("제목은 필수 입력 항목입니다."));
    }

    @Test
    @DisplayName("제목 길이 초과로 게시글 등록 실패")
    void failToPostWhenTitleExceedsLengthLimit() {
        // Given
        String longTitle = "a".repeat(51);
        PostRequest postRequest = new PostRequest();
        postRequest.setTitle(longTitle);
        postRequest.setWriter("작성자1");
        postRequest.setMail("test@example.com");
        postRequest.setPassword("1234");
        postRequest.setContent("테스트 내용입니다.");
        postRequest.setCategory(PostCategory.NOTICE);

        // When & Then
        assertThatThrownBy(() -> postService.createPost(postRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("제목은 30자 이하로 입력해주세요.");
    }

    @Test
    @DisplayName("비밀번호 형식이 맞지 않아 게시글 등록 실패")
    void failToPostWhenPasswordInvalid() {
        // Given
        PostRequest postRequest = new PostRequest();
        postRequest.setTitle("테스트 게시글 제목");
        postRequest.setWriter("작성자1");
        postRequest.setMail("test@example.com");
        postRequest.setPassword("abc");
        postRequest.setContent("테스트 내용입니다.");
        postRequest.setCategory(PostCategory.QNA);

        // When & Then
        assertThatThrownBy(() -> postService.createPost(postRequest))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("비밀번호는 최소 4자 이상이어야 합니다.");
    }
}

