package com.example.festimo.domain.post.service

import com.example.festimo.domain.meet.repository.CompanionMemberRepository
import com.example.festimo.domain.meet.repository.CompanionRepository
import com.example.festimo.domain.meet.service.CompanionService
import com.example.festimo.domain.post.dto.*
import com.example.festimo.domain.post.entity.Comment
import com.example.festimo.domain.post.entity.Post
import com.example.festimo.domain.post.entity.PostCategory
import com.example.festimo.domain.post.mapper.CommentMapper
import com.example.festimo.domain.post.mapper.PostMapper
import com.example.festimo.domain.post.repository.CommentRepository
import com.example.festimo.domain.post.repository.PostRepository
import com.example.festimo.domain.user.domain.User
import com.example.festimo.domain.user.repository.UserRepository
import com.example.festimo.exception.*
import com.example.festimo.global.dto.PageResponse
import org.hibernate.Hibernate
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.domain.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.time.LocalDateTime

@Service
@Validated
@EnableCaching
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val postMapper: PostMapper,
    private val commentMapper: CommentMapper,
    private val commentRepository: CommentRepository,
    private val companionService: CompanionService,
    private val companionRepository: CompanionRepository,
    private val companionMemberRepository: CompanionMemberRepository
) : PostService {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun createPost(request: PostRequest, authentication: Authentication) {
        val user = validateAuthenticationAndGetUser(authentication)

        val post = Post(
            title = request.title,
            nickname = user.nickname,
            mail = user.email,
            password = request.password,
            content = request.content,
            category = request.category,
            tags = request.tags?.toMutableSet() ?: mutableSetOf(),
            user = user
        )

        val savedPost = postRepository.save(post)

        if (savedPost.category == PostCategory.COMPANION) {
            savedPost.id?.let { postId ->
                companionService.createCompanion(postId, user.email)
            }
        }

        clearWeeklyTopPostsCache()
    }

    override fun getAllPosts(page: Int, size: Int): PageResponse<PostListResponse> {
        if (page < 1 || size <= 0) {
            throw InvalidPageRequest()
        }

        val pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val posts = postRepository.findAll(pageable)

        if (posts.isEmpty) {
            throw NoContent()
        }

        val responseList = posts.content.map { PostListResponse(it) }
        return PageResponse(PageImpl(responseList, pageable, posts.totalElements))
    }

    @Transactional
    override fun getPostById(postId: Long, incrementView: Boolean, authentication: Authentication): PostDetailResponse {
        val user = validateAuthenticationAndGetUser(authentication)
        val post = postRepository.findByIdWithDetails(postId) ?: throw PostNotFound()

        if (incrementView) {
            postRepository.incrementViews(postId)
            post.views += 1
        }

        return postMapper.postToPostDetailResponse(post).apply {
            owner = post.user?.id == user.id
            admin = user.role == User.Role.ADMIN
            liked = post.likedByUsers.any { it.id == user.id }
            comments = post.comments.map { comment ->
                commentMapper.toCommentResponse(comment).apply {
                    owner = comment.nickname == user.nickname
                    admin = user.role == User.Role.ADMIN
                }
            }
        }
    }

    @Transactional
    override fun updatePost(postId: Long, request: UpdatePostRequest): PostDetailResponse {
        val user = SecurityContextHolder.getContext().authentication?.name?.let { email ->
            userRepository.findByEmail(email).orElseThrow { UnauthorizedException() }
        } ?: throw UnauthorizedException()

        val post = postRepository.findById(postId).orElseThrow { PostNotFound() }

        if (request.password.isNullOrEmpty() || post.password != request.password) {
            throw InvalidPasswordException()
        }

        if (request.title == null && request.content == null && request.category == null) {
            throw IllegalArgumentException("수정할 필드가 없습니다.")
        }

        post.apply {
            title = request.title ?: title
            content = request.content ?: content
            category = request.category ?: category
        }

        postRepository.saveAndFlush(post)
        clearWeeklyTopPostsCache()

        return postMapper.postToPostDetailResponse(post).apply {
            owner = post.user?.id == user.id
            admin = user.role == User.Role.ADMIN
        }
    }

    @Transactional
    override fun deletePost(postId: Long, password: String, authentication: Authentication) {
        val user = validateAuthenticationAndGetUser(authentication)
        val post = postRepository.findById(postId).orElseThrow { PostNotFound() }

        when {
            post.user == user -> {
                if (post.password != password) throw InvalidPasswordException()
            }
            user.role != User.Role.ADMIN -> throw PostDeleteAuthorizationException()
        }

        if (post.category == PostCategory.COMPANION) {
            companionRepository.findByPost(post).orElse(null)?.let { companion ->
                companion.companionId?.let { id ->
                    companionMemberRepository.deleteByCompanion_CompanionId(id)
                    companionRepository.delete(companion)
                }
            }
        }

        postRepository.delete(post)
        clearWeeklyTopPostsCache()
    }

    @Cacheable(
        value = ["posts:weeklyTopPosts"],
        key = "#root.method.name + '_' + T(java.time.LocalDate).now().toString()",
        unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    override fun getCachedWeeklyTopPosts(): List<PostListResponse> {
        return try {
            val lastWeek = LocalDateTime.now().minusDays(7)
            postRepository.findTopPostsOfWeek(lastWeek)
                .map { post ->
                    Hibernate.initialize(post.tags)
                    PostListResponse(post)
                }
                .take(5)
        } catch (e: Exception) {
            logger.error("주간 인기 게시물 조회 중 오류 발생", e)
            emptyList()
        }
    }

    @CacheEvict(value = ["posts:weeklyTopPosts"])
    override fun clearWeeklyTopPostsCache() {
        // 캐시 초기화
    }

    override fun searchPosts(keyword: String): List<PostListResponse> =
        postRepository.searchPostsByKeyword(keyword)
            .map { PostListResponse(it) }

    override fun getComments(postId: Long): List<CommentResponse> {
        val user = SecurityContextHolder.getContext().authentication?.name?.let { email ->
            userRepository.findByEmail(email).orElseThrow { UnauthorizedException() }
        } ?: throw UnauthorizedException()

        val post = postRepository.findById(postId).orElseThrow { PostNotFound() }
        val comments = commentRepository.findByPostOrderBySequenceAsc(post)

        return comments.map { comment ->
            commentMapper.toCommentResponse(comment).apply {
                owner = comment.nickname == user.nickname
                admin = user.role == User.Role.ADMIN
            }
        }
    }

    @Transactional
    override fun createComment(postId: Long, request: CommentRequest, authentication: Authentication): CommentResponse {
        val user = validateAuthenticationAndGetUser(authentication)
        val post = postRepository.findById(postId).orElseThrow { PostNotFound() }

        val maxSequence = commentRepository.findMaxSequenceByPost(post)
        val nextSequence = maxSequence?.plus(1) ?: 1

        val comment = Comment(
            comment = request.comment,
            nickname = user.nickname,
            post = post,
            sequence = nextSequence
        )

        val savedComment = commentRepository.save(comment)

        return commentMapper.toCommentResponse(savedComment).apply {
            owner = true
            admin = user.role == User.Role.ADMIN
        }
    }

    @Transactional
    override fun updateComment(
        postId: Long,
        sequence: Int,
        request: UpdateCommentRequest,
        authentication: Authentication
    ): CommentResponse {
        val user = validateAuthenticationAndGetUser(authentication)

        if (!postRepository.existsById(postId)) {
            throw PostNotFound()
        }

        val comment = commentRepository.findByPostIdAndSequence(postId, sequence)
            ?: throw CommentNotFound()

        if (comment.nickname != user.nickname) {
            throw CommentUpdateAuthorizationException()
        }

        comment.updateContent(request.comment)
        val updatedComment = commentRepository.saveAndFlush(comment)

        return commentMapper.toCommentResponse(updatedComment)
    }

    @Transactional
    override fun deleteComment(postId: Long, sequence: Int, authentication: Authentication) {
        val user = validateAuthenticationAndGetUser(authentication)

        if (!postRepository.existsById(postId)) {
            throw PostNotFound()
        }

        val comment = commentRepository.findByPostIdAndSequence(postId, sequence)
            ?: throw CommentNotFound()

        if (comment.nickname != user.nickname && user.role != User.Role.ADMIN) {
            throw CommentDeleteAuthorizationException()
        }

        commentRepository.delete(comment)
    }

    @Transactional
    override fun toggleLike(postId: Long, authentication: Authentication): PostDetailResponse {
        val user = validateAuthenticationAndGetUser(authentication)
        val post = postRepository.findById(postId).orElseThrow { PostNotFound() }

        post.toggleLike(user)
        postRepository.save(post)

        return postMapper.postToPostDetailResponse(post)
    }

    private fun validateAuthenticationAndGetUser(authentication: Authentication): User {
        if (!authentication.isAuthenticated) {
            throw UnauthorizedException()
        }
        return userRepository.findByEmail(authentication.name)
            .orElseThrow { UnauthorizedException() }
    }
}