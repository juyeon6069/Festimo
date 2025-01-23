package com.example.festimo.domain.post.controller;

import com.example.festimo.domain.post.dto.PostListResponse;
import com.example.festimo.domain.post.dto.TagResponse;
import com.example.festimo.domain.post.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "이번 주 인기 태그 조회")
    @GetMapping("/popular")
    public ResponseEntity<List<TagResponse>> getWeeklyTopTags() {
        List<TagResponse> topTags = tagService.getTopWeeklyTags();
        return ResponseEntity.ok(topTags);
    }

    @Operation(summary = "태그로 게시글 검색")
    @GetMapping("/posts")
    public ResponseEntity<List<PostListResponse>> searchPostsByTag(
            @RequestParam String tag
    ) {
        List<PostListResponse> posts = tagService.searchByTag(tag);
        return ResponseEntity.ok(posts);
    }
}