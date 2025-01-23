package com.example.festimo.domain.post.service;

import com.example.festimo.domain.post.dto.PostListResponse;
import com.example.festimo.domain.post.dto.TagResponse;
import com.example.festimo.domain.post.entity.Post;
import com.example.festimo.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {
    private final PostRepository postRepository;

    // 이번 주 인기 태그 목록 조회
    @Cacheable(cacheNames = "popularTags")
    public List<TagResponse> getTopWeeklyTags() {
        try {
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            List<Post> recentPosts = postRepository.findByCreatedAtAfter(oneWeekAgo);

            Map<String, Long> tagCount = new HashMap<>();

            for (Post post : recentPosts) {
                if (post.getTags() != null && !post.getTags().isEmpty()) {
                    for (String tag : post.getTags()) {
                        tag = tag.trim();
                        tagCount.merge(tag, 1L, Long::sum);
                    }
                }
            }

            // 태그 사용 횟수 기준 내림차순 정렬, 상위 5개 선택(사용 횟수는 5 이상)
            return tagCount.entrySet().stream()
                    .filter(entry -> entry.getValue() >= 5)
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(entry -> new TagResponse(entry.getKey(), entry.getValue().intValue()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("인기 태그 조회 중 오류 발생", e);
            return Collections.emptyList();
        }
    }

    // 1시간마다 인기 태그 캐시 갱신
    @Scheduled(fixedRate = 3600000)
    @CacheEvict(cacheNames = "popularTags")
    public void refreshPopularTagsCache() {
        log.info("인기 태그 캐시 갱신 - {}", LocalDateTime.now());
    }


    // 특정 태그가 포함된 게시글 목록 검색
    public List<PostListResponse> searchByTag(String tag) {
        List<Post> posts = postRepository.findByTag(tag);
        return posts.stream()
                .map(PostListResponse::new)
                .collect(Collectors.toList());
    }
}