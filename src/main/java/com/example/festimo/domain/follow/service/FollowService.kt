package com.example.festimo.domain.follow.service

import com.example.festimo.domain.follow.domain.Follow
import com.example.festimo.domain.follow.repository.FollowRepository
import com.example.festimo.domain.user.repository.UserRepository
import com.example.festimo.exception.CustomException
import com.example.festimo.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository  // 기존의 UserRepository 사용
) {

    @Transactional
    fun followUser(followerId: Long, followeeId: Long): Follow {
        // 자기 자신을 팔로우할 수 없도록 체크
        if (followerId == followeeId) {
            throw CustomException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED)
        }
        // 이미 팔로우 관계가 존재하는지 확인
        if (followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId) != null) {
            throw CustomException(ErrorCode.ALREADY_FOLLOWED)
        }
        // UserRepository를 통해 사용자 조회 (없을 경우 예외 발생)
        val follower = userRepository.findById(followerId)
            .orElseThrow { CustomException(ErrorCode.FOLLOWER_NOT_FOUND) }
        val followee = userRepository.findById(followeeId)
            .orElseThrow { CustomException(ErrorCode.FOLLOWEE_NOT_FOUND) }

        val follow = Follow(follower = follower, followee = followee)
        return followRepository.save(follow)
    }

    @Transactional
    fun unfollowUser(followerId: Long, followeeId: Long) {
        val follow = followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
            ?: throw CustomException(ErrorCode.FOLLOW_RELATION_NOT_FOUND)
        followRepository.delete(follow)
    }

    // List<User> 반환
    @Transactional(readOnly = true)
    fun getFollowers(userId: Long) = followRepository.findAllByFolloweeId(userId).map { it.follower }

    @Transactional(readOnly = true)
    fun getFollowing(userId: Long) = followRepository.findAllByFollowerId(userId).map { it.followee }

    // 특정 사용자의 팔로잉(내가 팔로우한 수) 계산
    fun getFollowingCount(userId: Long): Long {
        return followRepository.countByFollowerId(userId)  // followerId 기준: 내가 팔로우한 사람 수
    }

    // 특정 사용자의 팔로워(나를 팔로우한 수) 계산
    fun getFollowersCount(userId: Long): Long {
        return followRepository.countByFolloweeId(userId)  // followeeId 기준: 나를 팔로우한 사람 수
    }
}
