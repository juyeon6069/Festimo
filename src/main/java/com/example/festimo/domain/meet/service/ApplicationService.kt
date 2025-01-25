package com.example.festimo.domain.meet.service

import com.example.festimo.domain.meet.dto.ApplicationResponse
import com.example.festimo.domain.meet.dto.LeaderApplicationResponse
import com.example.festimo.domain.meet.entity.Applications
import com.example.festimo.domain.meet.entity.CompanionMember
import com.example.festimo.domain.meet.entity.CompanionMemberId
import com.example.festimo.domain.meet.mapper.ApplicationMapper
import com.example.festimo.domain.meet.mapper.LeaderApplicationMapper
import com.example.festimo.domain.meet.repository.ApplicationRepository
import com.example.festimo.domain.meet.repository.CompanionMemberRepository
import com.example.festimo.domain.meet.repository.CompanionRepository
import com.example.festimo.domain.user.domain.User
import com.example.festimo.domain.user.repository.UserRepository
import com.example.festimo.exception.CustomException
import com.example.festimo.exception.ErrorCode.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ApplicationService(
    private val applicationRepository: ApplicationRepository,
    private val companionRepository: CompanionRepository,
    private val userRepository: UserRepository,
    private val companionMemberRepository: CompanionMemberRepository
) {

    private fun getUserFromEmail(email: String): User =
        userRepository.findByEmail(email)
            .orElseThrow { CustomException(USER_NOT_FOUND) }

    private fun validateLeaderAccess(companionId: Long, userId: Long) {
        val leaderId = companionRepository.findLeaderIdByCompanyId(companionId)
            .orElseThrow { CustomException(COMPANY_NOT_FOUND) }

        if (userId != leaderId) {
            throw CustomException(ACCESS_DENIED)
        }
    }

    private fun validateAndGetApplication(applicationId: Long): Applications {
        val application = applicationRepository.findById(applicationId)
            .orElseThrow { CustomException(APPLICATION_NOT_FOUND) }

        if (application.status != Applications.Status.PENDING) {
            throw CustomException(INVALID_APPLICATION_STATUS)
        }

        return application
    }

    @Transactional
    fun createApplication(email: String, postId: Long): ApplicationResponse {
        val user = getUserFromEmail(email)

        val companionId = companionRepository.findCompanionIdByPostId(postId)
            .orElseThrow { CustomException(POST_NOT_FOUND) }

        if (!companionRepository.existsById(companionId)) {
            throw CustomException(COMPANY_NOT_FOUND)
        }

        if (applicationRepository.existsByUserIdAndCompanionId(user.id, companionId)) {
            throw CustomException(DUPLICATE_APPLICATION)
        }

        val application = Applications(user.id, companionId)
        return ApplicationMapper.INSTANCE.toDto(applicationRepository.save(application))
    }

    @Transactional
    fun getAllApplications(companionId: Long, email: String): List<LeaderApplicationResponse> {
        val user = getUserFromEmail(email)
        validateLeaderAccess(companionId, user.id)

        val applications = applicationRepository.findByCompanionIdAndStatus(
            companionId,
            Applications.Status.PENDING
        )

        val userIds = applications.map { it.userId }

        val userNicknames = userRepository.findNicknamesByUserIds(userIds)
            .associate { it.userId to it.nickname }

        return applications.map { app ->
            LeaderApplicationMapper.INSTANCE.toDto(app, userNicknames[app.userId])
        }
    }

    @Transactional
    fun acceptApplication(applicationId: Long, email: String) {
        val leader = getUserFromEmail(email)
        val application = validateAndGetApplication(applicationId)
        validateLeaderAccess(application.companionId, leader.id)

        application.status = Applications.Status.ACCEPTED
        applicationRepository.save(application)

        val user = userRepository.findById(application.userId)
            .orElseThrow { CustomException(USER_NOT_FOUND) }

        val companionMember = createCompanionMember(application, user)
        companionMemberRepository.save(companionMember)
    }

    private fun createCompanionMember(application: Applications, user: User): CompanionMember {
        val companionMemberId = CompanionMemberId(
            application.companionId,
            user.id
        )

        return CompanionMember().apply {
            id = companionMemberId
            this.user = user
            joinedDate = LocalDateTime.now()
        }
    }

    @Transactional
    fun rejectApplication(applicationId: Long, email: String) {
        val leader = getUserFromEmail(email)
        val application = validateAndGetApplication(applicationId)
        validateLeaderAccess(application.companionId, leader.id)

        application.status = Applications.Status.REJECTED
        applicationRepository.save(application)
    }
}