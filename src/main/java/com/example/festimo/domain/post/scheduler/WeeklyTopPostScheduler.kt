package com.example.festimo.domain.post.scheduler;

import com.example.festimo.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class WeeklyTopPostScheduler {

    private final PostService postService;

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void updateWeeklyTopPosts() {
        try {
            postService.clearWeeklyTopPostsCache();
            postService.getCachedWeeklyTopPosts();
            log.info("주간 인기 게시글 캐시를 성공적으로 갱신했습니다.");
        } catch (Exception e) {
            log.error("주간 인기 게시글 캐시 갱신에 실패했습니다.", e);
        }
    }
}