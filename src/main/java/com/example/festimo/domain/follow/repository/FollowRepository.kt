package com.example.festimo.domain.follow.repository

import com.example.festimo.domain.follow.domain.Follow
import org.springframework.data.jpa.repository.JpaRepository


interface FollowRepository : JpaRepository<Follow, Long> {
    fun findByFollowerIdAndFolloweeId(followerId: Long, followeeId: Long): Follow?
    fun findAllByFollowerId(followerId: Long): List<Follow> // 팔로잉 목록
    fun findAllByFolloweeId(followeeId: Long): List<Follow> // 팔로워 목록

    // 팔로잉 수(내가 팔로우한 사용자 수) 계산
    fun countByFollowerId(followerId: Long): Long
    // 팔로워 수(나를 팔로우한 사용자 수) 계산
    fun countByFolloweeId(followeeId: Long): Long

}
