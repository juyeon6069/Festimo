package com.example.festimo.domain.meet.service

import com.example.festimo.domain.meet.dto.ApplicantReviewResponse
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
import com.example.festimo.domain.review.repository.ReviewRepository
import com.example.festimo.domain.user.domain.User
import com.example.festimo.domain.user.repository.UserRepository
import com.example.festimo.exception.CustomException
import com.example.festimo.exception.ErrorCode.*

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable



@Service
class ApplicationService(
    private val applicationRepository: ApplicationRepository,
    private val companionRepository: CompanionRepository,
    private val userRepository: UserRepository,
    private val companionMemberRepository: CompanionMemberRepository,
    private val reviewRepository: ReviewRepository
) {

    private fun getUserFromEmail(email: String): User =
        userRepository.findByEmail(email)
            ?: throw CustomException(USER_NOT_FOUND)

    private fun validateLeaderAccess(companionId: Long, userId: Long) {
        val leaderId = companionRepository.findLeaderIdByCompanyId(companionId)
            .orElseThrow { CustomException(COMPANION_NOT_FOUND) }

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
        val userId = user.id

        val companionId = companionRepository.findCompanionIdByPostId(postId)
            .orElseThrow { CustomException(POST_NOT_FOUND) }

        if (!companionRepository.existsById(companionId)) {
            throw CustomException(COMPANION_NOT_FOUND)
        }

        if (applicationRepository.existsByUserIdAndCompanionId(userId, companionId)) {
            throw CustomException(DUPLICATE_APPLICATION)
        }

        val application = Applications(userId, companionId)  // non-null Long 전달
        return ApplicationMapper.INSTANCE.toDto(applicationRepository.save(application))
    }

    /**
     * 신청 리스트 확인
     *
     * @param companionId 확인하려는 동행의 ID
     * @return 신청 리스트 정보
     */
    @Transactional
    fun getAllApplications(companionId: Long, email: String): List<LeaderApplicationResponse> {
        val user = getUserFromEmail(email)
        validateLeaderAccess(companionId, user.id)

        val applications = applicationRepository.findByCompanionIdAndStatus(
            companionId,
            Applications.Status.PENDING
        )

        val userIds = applications
            .map { it.userId }
            .toList()

        val users = userRepository.findApplicateInfoByUserIds(userIds)

        return LeaderApplicationMapper.INSTANCE.toDtoList(users)
    }

    @Transactional
    fun acceptApplication(applicationId: Long, email: String) {
        val leader = getUserFromEmail(email)
        val leaderId = leader.id

        val application = validateAndGetApplication(applicationId)
        validateLeaderAccess(application.companionId, leaderId)

        application.status = Applications.Status.ACCEPTED
        applicationRepository.save(application)

        val user = userRepository.findById(application.userId)
            .orElseThrow { CustomException(USER_NOT_FOUND) }

        val companionMember = createCompanionMember(application, user)
        companionMemberRepository.save(companionMember)
    }

    private fun createCompanionMember(application: Applications, user: User): CompanionMember {
        val userId = user.id

        val companionMemberId = CompanionMemberId(
            application.companionId,
            userId
        )

        // Companion 조회
        val companion = companionRepository.findById(application.companionId)
            .orElseThrow { CustomException(COMPANION_MEMBER_NOT_FOUND) }

        // CompanionMember 객체 생성 및 반환
        return CompanionMember(
            id = companionMemberId,
            companion = companion,
            user = user,
            joinedDate = LocalDateTime.now()
        )
    }


    @Transactional
    fun rejectApplication(applicationId: Long, email: String) {
        val leader = getUserFromEmail(email)
        val leaderId = leader.id

        val application = validateAndGetApplication(applicationId)
        validateLeaderAccess(application.companionId, leaderId)

        application.status = Applications.Status.REJECTED
        applicationRepository.save(application)
    }

    /**
     * 신청자 상세 정보
     *
     * @param applicationId 조회하고 싶은 applicationId
     */
    fun getApplicantReviews(applicationId: Long, pageable: Pageable): Page<ApplicantReviewResponse> {
        // applicationId로 userId 찾기
        val application = applicationRepository.findById(applicationId)
            .orElseThrow { CustomException(APPLICATION_NOT_FOUND) }

        val userId = application.userId

        // userId(revieweeId) 기준으로 리뷰 조회 (페이징 처리)
        val reviews = reviewRepository.findByRevieweeId(userId, pageable)

        return reviews.map { review ->
            ApplicantReviewResponse(review.rating, review.content)
        }
    }
}