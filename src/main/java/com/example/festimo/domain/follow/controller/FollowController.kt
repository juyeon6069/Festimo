package com.example.festimo.domain.follow.controller

import com.example.festimo.domain.follow.dto.FollowRequestDTO
import com.example.festimo.domain.follow.dto.FollowResponseDTO
import com.example.festimo.domain.follow.dto.UserDTO
import com.example.festimo.domain.follow.service.FollowService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/follow")
class FollowController(
    private val followService: FollowService
) {

    // 사용자 팔로우 요청 처리
    @PostMapping
    fun followUser(@RequestBody request: FollowRequestDTO): ResponseEntity<FollowResponseDTO> {
        val follow = followService.followUser(request.followerId, request.followeeId)
        val response = FollowResponseDTO(
            id = follow.id,
            followerId = follow.follower.id,
            followeeId = follow.followee.id,
            createdAt = follow.createdAt
        )
        return ResponseEntity.ok(response)
    }

    // 언팔로우 요청 처리
    @DeleteMapping
    fun unfollowUser(
        @RequestParam followerId: Long,
        @RequestParam followeeId: Long
    ): ResponseEntity<Void> {
        followService.unfollowUser(followerId, followeeId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/check")
    fun checkFollow(
        @RequestParam followerId: Long,
        @RequestParam followeeId: Long
    ): ResponseEntity<Boolean> {
        val isFollowing = followService.checkFollow(followerId, followeeId)
        return ResponseEntity.ok(isFollowing)
    }


    // 특정 사용자의 팔로워 목록 조회
    @GetMapping("/followers/{userId}")
    fun getFollowers(@PathVariable userId: Long): ResponseEntity<List<UserDTO>> {
        val followers = followService.getFollowers(userId).map { UserDTO.from(it) }
        return ResponseEntity.ok(followers)
    }

    // 특정 사용자의 팔로잉 목록 조회
    @GetMapping("/following/{userId}")
    fun getFollowing(@PathVariable userId: Long): ResponseEntity<List<UserDTO>> {
        val following = followService.getFollowing(userId).map { UserDTO.from(it) }
        return ResponseEntity.ok(following)
    }

    // 특정 사용자의 팔로워 수를 반환
    @GetMapping("/followers/count")
    fun getFollowersCount(@RequestParam userId: Long): ResponseEntity<Long> {
        return ResponseEntity.ok(followService.getFollowersCount(userId))
    }

    // 특정 사용자의 팔로잉 수를 반환
    @GetMapping("/following/count")
    fun getFollowingCount(@RequestParam userId: Long): ResponseEntity<Long> {
        return ResponseEntity.ok(followService.getFollowingCount(userId))
    }
}
