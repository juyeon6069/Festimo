package com.example.festimo.domain.post.service;

import com.example.festimo.domain.meet.entity.Companion;
import com.example.festimo.domain.meet.repository.CompanionMemberRepository;
import com.example.festimo.domain.meet.repository.CompanionRepository;
import com.example.festimo.domain.meet.service.CompanionService;
import com.example.festimo.domain.post.dto.*;
import com.example.festimo.domain.post.entity.Comment;
import com.example.festimo.domain.post.entity.Post;
import com.example.festimo.domain.post.entity.PostCategory;
import com.example.festimo.domain.post.repository.CommentRepository;
import com.example.festimo.domain.post.repository.PostRepository;
import com.example.festimo.domain.user.domain.User;
import com.example.festimo.domain.user.repository.UserRepository;
import com.example.festimo.exception.*;
import com.example.festimo.global.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@EnableCaching
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final CommentRepository commentRepository;
    private final CompanionService companionService;
    private final CompanionRepository companionRepository;
    private final CompanionMemberRepository companionMemberRepository;

    // 게시글 등록
    @Transactional
    @Override
    public void createPost(
            @Valid PostRequest request,
            Authentication authentication) {
        User user = validateAuthenticationAndGetUser(authentication);

        Post post = Post.builder()
                .title(request.getTitle())
                .nickname(user.getNickname())
                .mail(user.getEmail())
                .password(request.getPassword())
                .content(request.getContent())
                .category(request.getCategory())
                .tags(request.getTags() != null ? new HashSet<>(request.getTags()) : new HashSet<>())
                .user(user)
                .build();

        Post savedPost = postRepository.save(post);

        // 카테고리가 COMPANION인 경우 동행 생성
        if (savedPost.getCategory() == PostCategory.COMPANION) {
            companionService.createCompanion(savedPost.getId(), user.getEmail());
        }

        clearWeeklyTopPostsCache();
    }

    // 게시글 전체 조회
    @Override
    public PageResponse<PostListResponse> getAllPosts(int page, int size) {
        if (page < 1 || size <= 0) {
            throw new InvalidPageRequest();
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Post> posts = postRepository.findAll(pageable);

        if (posts.isEmpty()) {
            throw new NoContent();
        }

        List<PostListResponse> responseList = posts.stream()
                .map(PostListResponse::new)
                .collect(Collectors.toList());

        return new PageResponse<>(new PageImpl<>(responseList, pageable, posts.getTotalElements()));
    }

    // 게시글 상세 조회
    @Transactional
    @Override
    public PostDetailResponse getPostById(Long postId, boolean incrementView, Authentication authentication) {
        User user = validateAuthenticationAndGetUser(authentication);

        Post post = postRepository.findByIdWithDetails(postId).orElseThrow(PostNotFound::new);

        if (incrementView) {
            postRepository.incrementViews(postId);
            post.setViews(post.getViews() + 1); // 클라이언트로 즉시 반영
        }

        boolean isOwner = post.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().equals(User.Role.ADMIN);
        boolean isLiked = post.getLikedByUsers().stream()
                .anyMatch(likedUser -> likedUser.getId().equals(user.getId()));


        List<CommentResponse> comments = post.getComments().stream()
                .map(comment -> new CommentResponse(
                        comment.getSequence(),
                        comment.getComment(),
                        comment.getNickname(),
                        post.getId(),
                        comment.getCreatedAt(),
                        comment.getUpdatedAt(),
                        comment.getNickname().equals(user.getNickname()),
                        isAdmin
                ))
                .collect(Collectors.toList());

        PostDetailResponse response = modelMapper.map(post, PostDetailResponse.class);
        response.setOwner(isOwner);
        response.setAdmin(isAdmin);
        response.setComments(comments);
        response.setTags(post.getTags());
        response.setReplies(comments.size());
        response.setLiked(isLiked);
        return response;
    }

    // 게시글 수정
    @Transactional
    @Override
    public PostDetailResponse updatePost(Long postId, @Valid UpdatePostRequest request) {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(UnauthorizedException::new);

        Post post = postRepository.findById(postId).orElseThrow(PostNotFound::new);

        if (ObjectUtils.isEmpty(request.getPassword()) || !post.getPassword().equals(request.getPassword())) {
            throw new InvalidPasswordException();
        }

        if (request.getTitle() == null && request.getContent() == null && request.getCategory() == null) {
            throw new IllegalArgumentException("수정할 필드가 없습니다.");
        }

        String newTitle = request.getTitle() != null ? request.getTitle() : post.getTitle();
        String newContent = request.getContent() != null ? request.getContent() : post.getContent();
        PostCategory newCategory = request.getCategory() != null ? request.getCategory() : post.getCategory();

        post.update(newTitle, newContent, newCategory);
        postRepository.saveAndFlush(post);

        PostDetailResponse response = modelMapper.map(post, PostDetailResponse.class);
        response.setOwner(post.getUser().getId().equals(user.getId()));
        response.setAdmin(user.getRole().equals(User.Role.ADMIN));

        clearWeeklyTopPostsCache();
        return response;
    }

    @Transactional
    @Override
    public void deletePost(Long postId, String password, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(UnauthorizedException::new);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFound::new);

        // 1. 게시글 작성자/비밀번호 체크
        if (post.getUser().equals(user)) {
            if (!post.getPassword().equals(password)) {
                throw new InvalidPasswordException();
            }
        } else if (!user.getRole().equals(User.Role.ADMIN)) {
            throw new PostDeleteAuthorizationException();
        }

        // 2. 게시글이 동행 카테고리인 경우에만 동행 데이터 삭제
        if (post.getCategory() == PostCategory.COMPANION) {
            Companion companion = companionRepository.findByPost(post)
                    .orElse(null);
            if (companion != null) {
                // companion_member 테이블의 관련 데이터 삭제
                companionMemberRepository.deleteByCompanion_CompanionId(companion.getCompanionId());
                // companion 테이블의 데이터 삭제
                companionRepository.delete(companion);
            }
        }

        // 3. 게시글 삭제
        postRepository.delete(post);
        clearWeeklyTopPostsCache();
    }

    // 주간 인기 게시글
    @Cacheable(value = "posts:weeklyTopPosts",
            key = "#root.method.name + '_' + T(java.time.LocalDate).now().toString()",
            unless = "#result == null || #result.isEmpty()")
    @Transactional(readOnly = true)
    public List<PostListResponse> getCachedWeeklyTopPosts() {
        try {
            LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
            List<Post> posts = postRepository.findTopPostsOfWeek(lastWeek);

            return posts.stream()
                    .map(post -> {
                        Hibernate.initialize(post.getTags());
                        return new PostListResponse(post);
                    })
                    .limit(5)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("주간 인기 게시물 조회 중 오류 발생", e);
            return Collections.emptyList();
        }
    }

    @CacheEvict(value = "posts:weeklyTopPosts")
    public void clearWeeklyTopPostsCache() {
        // 캐시 초기화
    }

    // 게시글 검색
    @Override
    public List<PostListResponse> searchPosts(String keyword) {
        return postRepository.searchPostsByKeyword(keyword)
                .stream()
                .map(PostListResponse::new)
                .collect(Collectors.toList());
    }

    // 댓글 목록 조회
    @Override
    public List<CommentResponse> getComments(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFound::new);
        List<Comment> comments = commentRepository.findByPostOrderBySequenceAsc(post);

        String currentUserNickname = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserNickname).orElseThrow(UnauthorizedException::new);

        return comments.stream()
                .map(comment -> {
                    CommentResponse response = modelMapper.map(comment, CommentResponse.class);
                    // 현재 사용자가 댓글 작성자인지
                    response.setOwner(comment.getNickname().equals(currentUser.getNickname()));
                    // 현재 사용자가 관리자인지
                    response.setAdmin(currentUser.getRole().equals(User.Role.ADMIN));
                    return response;
                })
                .collect(Collectors.toList());
    }

    // 댓글 등록
    @Transactional
    @Override
    public CommentResponse createComment(Long postId, @Valid CommentRequest request, Authentication authentication) {
        User user = validateAuthenticationAndGetUser(authentication);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFound::new);

        Integer maxSequence = commentRepository.findMaxSequenceByPost(post);
        Integer nextSequence = (maxSequence == null) ? 1 : maxSequence + 1;

        Comment comment = Comment.builder()
                .comment(request.getComment())
                .nickname(user.getNickname())
                .post(post)
                .sequence(nextSequence)
                .build();
        commentRepository.save(comment);

        CommentResponse response = modelMapper.map(comment, CommentResponse.class);
        response.setOwner(true);  // 새로 작성한 댓글은 현재 사용자가 작성자
        response.setAdmin(user.getRole().equals(User.Role.ADMIN));

        return modelMapper.map(comment, CommentResponse.class);
    }

    // 댓글 수정
    @Transactional
    @Override
    public CommentResponse updateComment(Long postId, Integer sequence, @Valid UpdateCommentRequest request, Authentication authentication) {
        User user = validateAuthenticationAndGetUser(authentication);

        if (!postRepository.existsById(postId)) {
            throw new PostNotFound();
        }

        Comment comment = commentRepository.findByPostIdAndSequence(postId, sequence).orElseThrow(CommentNotFound::new);

        if (!comment.getNickname().equals(user.getNickname())) {
            throw new CommentUpdateAuthorizationException();
        }

        comment.updateContent(request.getComment());
        commentRepository.saveAndFlush(comment);

        return modelMapper.map(comment, CommentResponse.class);
    }

    private User validateAuthenticationAndGetUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException();
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(UnauthorizedException::new);
    }

    // 댓글 삭제
    @Transactional
    @Override
    public void deleteComment(Long postId, Integer sequence, Authentication authentication) {
        User user = validateAuthenticationAndGetUser(authentication);

        if (!postRepository.existsById(postId)) {
            throw new PostNotFound();
        }

        Comment comment = commentRepository.findByPostIdAndSequence(postId, sequence).orElseThrow(CommentNotFound::new);

        if (!comment.getNickname().equals(user.getNickname()) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new CommentDeleteAuthorizationException();
        }

        commentRepository.delete(comment);
    }

    // 좋아요
    @Transactional
    @Override
    public PostDetailResponse toggleLike(Long postId, Authentication authentication) {
        User user = validateAuthenticationAndGetUser(authentication);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFound::new);

        post.toggleLike(user);
        postRepository.save(post);

        return modelMapper.map(post, PostDetailResponse.class);
    }
}