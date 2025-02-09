package com.example.festimo.domain.post.scheduler

import com.example.festimo.domain.post.service.PostService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class WeeklyTopPostScheduler(private val postService: PostService) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 3600000)
    fun updateWeeklyTopPosts() {
        try {
            postService.clearWeeklyTopPostsCache()
            postService.getCachedWeeklyTopPosts()
            logger.info("주간 인기 게시글 캐시를 성공적으로 갱신했습니다.")
        } catch (e: Exception) {
            logger.error("주간 인기 게시글 캐시 갱신에 실패했습니다.", e)
        }
    }
}