package com.example.festimo.domain.meet.service

import com.example.festimo.domain.meet.dto.CompanionResponse
import com.example.festimo.domain.meet.entity.Companion
import com.example.festimo.domain.meet.entity.CompanionMember
import com.example.festimo.domain.meet.entity.CompanionMemberId
import com.example.festimo.domain.meet.entity.CompanionStatus
import com.example.festimo.domain.meet.repository.CompanionMemberRepository
import com.example.festimo.domain.meet.repository.CompanionRepository
import com.example.festimo.domain.post.repository.PostRepository
import com.example.festimo.domain.user.domain.User
import com.example.festimo.domain.user.repository.UserRepository
import com.example.festimo.exception.CompanionNotFoundException
import com.example.festimo.exception.CustomException
import com.example.festimo.exception.ErrorCode.*

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
@Service
class CompanionService(
    private val companionMemberRepository: CompanionMemberRepository,
    private val postRepository: PostRepository,
    private val companionRepository: CompanionRepository,
    private val userRepository: UserRepository
) {
    private fun getUserFromEmail(email: String): User =
        userRepository.findByEmail(email) ?: throw CustomException(USER_NOT_FOUND)

    private fun validateLeaderAccess(companionId: Long, userId: Long) {
        val leaderId = companionRepository.findLeaderIdByCompanyId(companionId)
            ?: throw CustomException(COMPANION_NOT_FOUND)

        if (userId != leaderId) {
            throw CustomException(ACCESS_DENIED)
        }
    }

    @Transactional
    fun createCompanion(postId: Long, email: String) {
        val user = getUserFromEmail(email)

        // post_id 검사
        val post = postRepository.findById(postId)
            .orElseThrow { CustomException(POST_NOT_FOUND) }

        // 중복 생성 방지
        if (companionRepository.findByPost(post) != null) {
            throw CustomException(COMPANION_ALREADY_EXISTS)
        }

        // companion 추가
        val now = LocalDateTime.now()
        val companion = Companion(
            companionId = 0,
            leaderId = user.id,
            companionDate = now,
            post = post,
            title = "동행",
            status = CompanionStatus.ONGOING
        )
        val savedCompanion = companionRepository.save(companion)

        // companion_member 추가
        addLeaderToCompanionMember(savedCompanion.companionId, user.id)
    }

    private fun addLeaderToCompanionMember(companionId: Long, userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { CustomException(USER_NOT_FOUND) }
        val companion = companionRepository.findById(companionId)
            .orElseThrow { CustomException(COMPANION_MEMBER_NOT_FOUND) }

        val companionMemberId = CompanionMemberId(companionId, userId)
        val companionMember = CompanionMember(
            id = companionMemberId,
            companion = companion,
            user = user,
            joinedDate = LocalDateTime.now()
        )
        companionMemberRepository.save(companionMember)
    }

    @Transactional
    fun deleteCompanion(companionId: Long, email: String) {
        val user = getUserFromEmail(email)
        val userId = user.id

        val companionMemberId = CompanionMemberId(companionId, userId)

        if (!companionMemberRepository.existsById(companionMemberId)) {
            throw CustomException(COMPANION_MEMBER_NOT_FOUND)
        }
        companionMemberRepository.deleteById(companionMemberId)
    }

    @Transactional(readOnly = true)
    fun getCompanionAsLeader(leaderId: Long): List<CompanionResponse> =
        companionRepository.findByLeaderId(leaderId)
            .map(::mapToCompanionResponse)
            .orEmpty()

    fun getCompanionAsMember(userId: Long): List<CompanionResponse> {
        val members = companionMemberRepository.findByUserId(userId)

        return members
            .filter {
                val hasCompanion = it.companion != null
                val hasUser = it.user != null
                hasCompanion && hasUser
            }
            .filter {
                val companionLeaderId = it.companion?.leaderId
                companionLeaderId != userId
            }
            .map { mapToCompanionResponse(it.companion!!) }
    }

    private fun mapToCompanionResponse(companion: Companion): CompanionResponse {
        val leader = companionRepository.findLeaderById(companion.leaderId!!)
        val members = companionMemberRepository.findMembersByCompanionId(companion.companionId)

        return CompanionResponse(
            title = companion.title,
            companionId = companion.companionId,
            leaderId = leader.userId,
            leaderName = leader.userName,
            status = companion.status,
            members = members
        )
    }

    @Transactional
    fun updateTitle(companionId: Long, title: String, email: String) {
        val user = getUserFromEmail(email)
        validateLeaderAccess(companionId, user.id)

        val companion = companionRepository.findById(companionId)
            .orElseThrow { CustomException(COMPANION_NOT_FOUND) }

        companion.changeTitle(title)
    }

    @Transactional
    fun completeCompanion(companionId: Long, email: String) {
        val user = getUserFromEmail(email)
        validateLeaderAccess(companionId, user.id)

        val companion = companionRepository.findById(companionId)
            .orElseThrow { CompanionNotFoundException() }

        if (companion.status == CompanionStatus.COMPLETED) {
            throw CustomException(ALREADY_COMPLETED)
        }

        companion.changeStatus(CompanionStatus.COMPLETED)
    }

    @Transactional
    fun restoreCompanion(companionId: Long, email: String) {
        val user = getUserFromEmail(email)
        validateLeaderAccess(companionId, user.id)

        val companion = companionRepository.findById(companionId)
            .orElseThrow { CompanionNotFoundException() }

        if (companion.status == CompanionStatus.ONGOING) {
            throw CustomException(ALREADY_ONGOING)
        }

        companion.changeStatus(CompanionStatus.ONGOING)
    }
}
