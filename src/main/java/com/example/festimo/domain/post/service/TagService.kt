package com.example.festimo.domain.post.service

import com.example.festimo.domain.post.dto.PostListResponse
import com.example.festimo.domain.post.dto.TagResponse
import com.example.festimo.domain.post.repository.PostRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class TagService(private val postRepository: PostRepository) {

    // 이번 주 인기 태그 목록 조회
    @Cacheable(value = ["popularTags"], key = "'weeklyTags'", unless = "#result.isEmpty()")
    fun getTopWeeklyTags(): List<TagResponse> {
        val oneWeekAgo = LocalDateTime.now().minusWeeks(1)
        val recentPosts = postRepository.findByCreatedAtAfter(oneWeekAgo)

        val tagCount = recentPosts
            .flatMap { it.tags }
            .groupingBy { it.trim() }
            .eachCount()
            .filter { it.value >= 5 } // 5번 이상 사용된 태그만 필터링

        return tagCount.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { TagResponse(it.key, it.value) }
    }

    // 1시간마다 인기 태그 캐시 갱신
    @CacheEvict(value = ["popularTags"], allEntries = true)
    @Scheduled(fixedRate = 3600000)
    fun refreshPopularTagsCache() {}

    // 특정 태그가 포함된 게시글 목록 검색
    fun searchByTag(tag: String): List<PostListResponse> =
        postRepository.findByTag(tag).map { PostListResponse(it) }
}