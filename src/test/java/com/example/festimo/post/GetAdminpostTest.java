package com.example.festimo.post;

import com.example.festimo.domain.post.controller.PostController;
import com.example.festimo.domain.post.dto.PostListResponse;
import com.example.festimo.domain.post.service.PostService;
import com.example.festimo.exception.InvalidPageRequest;
import com.example.festimo.exception.NoContent;
import com.example.festimo.global.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GetAdminpostTest {

    private MockMvc mockMvc;

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
    }

    @Test
    @DisplayName("전체 게시글 조회")
    public void testGetAllPosts() throws Exception {
        // Given
        List<PostListResponse> postListRespons = List.of(
                new PostListResponse(1L, "Title 1", "Writer 1", 1, "2024-12-17"),
                new PostListResponse(2L, "Title 2", "Writer 2", 2, "2024-12-18")
        );

        PageResponse<PostListResponse> pageResponse = new PageResponse<>(
                new PageImpl<>(postListRespons, PageRequest.of(0, 10), 20)
        );

        when(postService.getAllPosts(Mockito.anyInt(), Mockito.anyInt())).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/companions?page=1&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Title 1"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("Title 2"))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(20))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    @DisplayName("페이지 또는 크기 값이 잘못된 경우")
    void testInvalidPageRequestException() {
        // Given
        int invalidPage = 0;
        int invalidSize = -1;

        // When
        when(postService.getAllPosts(invalidPage, invalidSize)).thenThrow(new InvalidPageRequest());

        // Then
        assertThatThrownBy(() -> postService.getAllPosts(invalidPage, invalidSize))
                .isInstanceOf(InvalidPageRequest.class)
                .hasMessageContaining("페이지 번호는 1 이상, 페이지 크기는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("게시글이 없는 경우")
    void testNoContentException() {
        // Given
        int validPage = 1;
        int validSize = 10;

        // When
        when(postService.getAllPosts(validPage, validSize)).thenThrow(new NoContent());

        // Then
        assertThatThrownBy(() -> postService.getAllPosts(validPage, validSize))
                .isInstanceOf(NoContent.class)
                .hasMessageContaining("조회된 데이터가 없습니다.");
    }
}

