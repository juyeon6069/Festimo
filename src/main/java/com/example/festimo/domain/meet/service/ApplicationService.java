package com.example.festimo.domain.meet.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.festimo.domain.meet.entity.CompanionMemberId;
import com.example.festimo.domain.meet.entity.CompanionMember;
import com.example.festimo.domain.meet.repository.CompanionMemberRepository;
import com.example.festimo.domain.meet.dto.ApplicationResponse;
import com.example.festimo.domain.meet.dto.LeaderApplicationResponse;
import com.example.festimo.domain.meet.entity.Applications;
import com.example.festimo.domain.meet.mapper.ApplicationMapper;
import com.example.festimo.domain.meet.mapper.LeaderApplicationMapper;
import com.example.festimo.domain.meet.repository.ApplicationRepository;
import com.example.festimo.domain.meet.repository.CompanionRepository;
import com.example.festimo.domain.user.domain.User;
import com.example.festimo.domain.user.repository.UserRepository;
import com.example.festimo.exception.CustomException;
import com.example.festimo.domain.user.dto.UserNicknameProjection;


import static com.example.festimo.exception.ErrorCode.*;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CompanionRepository companionRepository;
    private final UserRepository userRepository;
    private final CompanionMemberRepository companionMemberRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            CompanionRepository companionRepository,
            UserRepository userRepository,
            CompanionMemberRepository companionMemberRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.companionRepository = companionRepository;
        this.userRepository = userRepository;
        this.companionMemberRepository = companionMemberRepository;
    }

    private User getUserFromEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private void validateLeaderAccess(Long companionId, Long userId) {
        Long leaderId = companionRepository.findLeaderIdByCompanyId(companionId)
                .orElseThrow(() -> new CustomException(COMPANY_NOT_FOUND));

        if (!userId.equals(leaderId)) {
            throw new CustomException(ACCESS_DENIED);
        }
    }

    private Applications validateAndGetApplication(Long applicationId) {
        Applications application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(APPLICATION_NOT_FOUND));

        if (!application.getStatus().equals(Applications.Status.PENDING)) {
            throw new CustomException(INVALID_APPLICATION_STATUS);
        }

        return application;
    }

    /**
     * 신청 생성
     *
     * @param email    신청을 생성하는 유저의 email
     * @param postId 신청 동행 ID
     * @return 생성된 신청 정보
     */
    @Transactional
    public ApplicationResponse createApplication(String email, Long postId) {
        User user = getUserFromEmail(email);

        Long companionId = companionRepository.findCompanionIdByPostId(postId)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

        if (!companionRepository.existsById(companionId)) {
            throw new CustomException(COMPANY_NOT_FOUND);
        }

        if (applicationRepository.existsByUserIdAndCompanionId(user.getId(), companionId)) {
            throw new CustomException(DUPLICATE_APPLICATION);
        }

        Applications application = new Applications(user.getId(), companionId);
        return ApplicationMapper.INSTANCE.toDto(applicationRepository.save(application));
    }

    /**
     * 신청 리스트 확인
     *
     * @param companionId 확인하려는 동행의 ID
     * @return 신청 리스트 정보
     */
    @Transactional
    public List<LeaderApplicationResponse> getAllApplications(Long companionId, String email) {
        User user = getUserFromEmail(email);
        validateLeaderAccess(companionId, user.getId());

        List<Applications> applications = applicationRepository.findByCompanionIdAndStatus(
                companionId,
                Applications.Status.PENDING
        );

        List<Long> userIds = applications.stream()
                .map(Applications::getUserId)
                .collect(Collectors.toList());

        Map<Long, String> userNicknames = userRepository.findNicknamesByUserIds(userIds)
                .stream()
                .collect(Collectors.toMap(
                        UserNicknameProjection::getUserId,
                        UserNicknameProjection::getNickname
                ));

        return applications.stream()
                .map(app -> LeaderApplicationMapper.INSTANCE.toDto(app, userNicknames.get(app.getUserId())))
                .collect(Collectors.toList());
    }

    /**
     * 리더의 신청 승인
     *
     * @param applicationId 승인하고 싶은 신청 ID
     * @param email 신청 승인하는 유저의 email
     */
    @Transactional
    public void acceptApplication(Long applicationId, String email) {
        User leader = getUserFromEmail(email);
        Applications application = validateAndGetApplication(applicationId);
        validateLeaderAccess(application.getCompanionId(), leader.getId());

        application.setStatus(Applications.Status.ACCEPTED);
        applicationRepository.save(application);

        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        CompanionMember companionMember = createCompanionMember(application, user);
        companionMemberRepository.save(companionMember);
    }

    private CompanionMember createCompanionMember(Applications application, User user) {
        CompanionMemberId companionMemberId = new CompanionMemberId(
                application.getCompanionId(),
                user.getId()
        );

        CompanionMember companionMember = new CompanionMember();
        companionMember.setId(companionMemberId);
        companionMember.setUser(user);
        companionMember.setJoinedDate(LocalDateTime.now());

        return companionMember;
    }

    /**
     * 리더의 신청 거절
     *
     * @param applicationId 거절하고 싶은 신청 ID
     * @param email 신청 승인하는 유저의 email
     */
    @Transactional
    public void rejectApplication(Long applicationId, String email) {
        User leader = getUserFromEmail(email);
        Applications application = validateAndGetApplication(applicationId);
        validateLeaderAccess(application.getCompanionId(), leader.getId());

        application.setStatus(Applications.Status.REJECTED);
        applicationRepository.save(application);
    }
}