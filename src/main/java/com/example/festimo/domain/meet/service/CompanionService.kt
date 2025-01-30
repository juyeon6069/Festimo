package com.example.festimo.domain.meet.service

import com.example.festimo.domain.meet.dto.CompanionResponse
import com.example.festimo.domain.meet.entity.Companion
import com.example.festimo.domain.meet.entity.CompanionMember
import com.example.festimo.domain.meet.entity.CompanionMemberId
import com.example.festimo.domain.meet.repository.CompanionMemberRepository
import com.example.festimo.domain.meet.repository.CompanionRepository
import com.example.festimo.domain.post.repository.PostRepository
import com.example.festimo.domain.user.domain.User
import com.example.festimo.domain.user.repository.UserRepository
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
        userRepository.findByEmail(email)
            ?.orElseThrow { CustomException(USER_NOT_FOUND) }
            ?: throw CustomException(USER_NOT_FOUND)

    @Transactional
    fun createCompanion(postId: Long, email: String) {
        val user = getUserFromEmail(email)
        val userId = user.id ?: throw CustomException(USER_NOT_FOUND)

        val post = postRepository.findById(postId)
            .orElseThrow { CustomException(POST_NOT_FOUND) }

        companionRepository.findByPost(post)
            .ifPresent { throw CustomException(COMPANION_ALREADY_EXISTS) }

        val companion = Companion(leaderId = userId, companionDate = LocalDateTime.now(), post = post)
        val savedCompanion = companionRepository.save(companion)
        addLeaderToCompanionMember(savedCompanion.companionId, userId)
    }

    private fun addLeaderToCompanionMember(companionId: Long, userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { CustomException(USER_NOT_FOUND) }
        val companion = companionRepository.findById(companionId)
            .orElseThrow { CustomException(COMPANION_NOT_FOUND) }

        // id 초기화를 명시적으로
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
        val userId = user.id ?: throw CustomException(USER_NOT_FOUND)

        val companionMemberId = CompanionMemberId(companionId, userId)

        if (!companionMemberRepository.existsById(companionMemberId)) {
            throw CustomException(COMPANION_NOT_FOUND)
        }
        companionMemberRepository.deleteById(companionMemberId)
    }

    @Transactional(readOnly = true)
    fun getCompanionAsLeader(leaderId: Long): List<CompanionResponse> =
        companionRepository.findByLeaderId(leaderId)
            ?.map(::mapToCompanionResponse)
            ?: emptyList()

    fun getCompanionAsMember(userId: Long): List<CompanionResponse> {
        val members = companionMemberRepository.findByUserId(userId)


        return members
            .filter {
                val hasCompanion = it.companion != null
                val hasUser = it.user != null
                hasCompanion && hasUser
            }
            .onEach {
                val companionId = it.companion?.companionId
                val userId = it.user?.id
            }
            .filter {
                val companionLeaderId = it.companion?.leaderId
                companionLeaderId != userId
            }
            .map { mapToCompanionResponse(it.companion!!) }
    }

    private fun mapToCompanionResponse(companion: Companion): CompanionResponse {

        // 리더 정보 로드
        val leader = companionRepository.findLeaderById(companion.leaderId!!)


        // 멤버 정보 로드
        val members = companionMemberRepository.findMembersByCompanionId(companion.companionId)


        return CompanionResponse(
            companionId = companion.companionId,
            leaderId = leader.userId,
            leaderName = leader.userName,
            members = members
        )
    }








}