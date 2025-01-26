package com.example.festimo.domain.post.controller

import com.example.festimo.domain.post.dto.PostListResponse
import com.example.festimo.domain.post.dto.TagResponse
import com.example.festimo.domain.post.service.TagService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tags")
class TagController(private val tagService: TagService) {

    @Operation(summary = "이번 주 인기 태그 조회")
    @GetMapping("/popular")
    fun getWeeklyTopTags(): ResponseEntity<List<TagResponse>> =
        ResponseEntity.ok(tagService.getTopWeeklyTags())

    @Operation(summary = "태그로 게시글 검색")
    @GetMapping("/posts")
    fun searchPostsByTag(@RequestParam tag: String): ResponseEntity<List<PostListResponse>> =
        ResponseEntity.ok(tagService.searchByTag(tag))
}