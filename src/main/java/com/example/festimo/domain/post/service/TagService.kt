package com.example.festimo.domain.post.service

import com.example.festimo.domain.post.dto.PostListResponse
import com.example.festimo.domain.post.dto.TagResponse
import com.example.festimo.domain.post.repository.PostRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class TagService(private val postRepository: PostRepository) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // 이번 주 인기 태그 목록 조회
    @Cacheable(cacheNames = ["popularTags"])
    fun getTopWeeklyTags(): List<TagResponse> {
        return try {
            val oneWeekAgo = LocalDateTime.now().minusWeeks(1)
            val recentPosts = postRepository.findByCreatedAtAfter(oneWeekAgo)
            val tagCount = mutableMapOf<String, Long>()

            recentPosts.forEach { post ->
                post.tags.forEach { tag ->
                    val trimmedTag = tag.trim()
                    tagCount[trimmedTag] = tagCount.getOrDefault(trimmedTag, 0L) + 1
                }
            }

            // 태그 사용 횟수를 기준으로 내림차순 정렬 후, 사용 횟수가 5 이상인 태그 상위 5개를 선택
            tagCount.entries.asSequence()
                .filter { it.value >= 5 }
                .sortedByDescending { it.value }
                .take(5)
                .map { TagResponse(it.key, it.value.toInt()) }
                .toList()
        } catch (e: Exception) {
            logger.error("인기 태그 조회 중 오류 발생", e)
            emptyList()
        }
    }

    // 1시간마다 인기 태그 캐시 갱신
    @Scheduled(fixedRate = 3600000)
    @CacheEvict(cacheNames = ["popularTags"])
    fun refreshPopularTagsCache() {
        logger.info("인기 태그 캐시 갱신 - {}", LocalDateTime.now())
    }

    // 특정 태그가 포함된 게시글 목록 검색
    fun searchByTag(tag: String): List<PostListResponse> =
        postRepository.findByTag(tag).map { PostListResponse(it) }
}