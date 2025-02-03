package com.example.festimo.domain.follow.domain

import com.example.festimo.domain.user.domain.User
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(
    name = "follows",
    uniqueConstraints = [UniqueConstraint(columnNames = ["follower_id", "followee_id"])] // 한 사용자가 동일한 대상을 중복해서 팔로우 하는것을 방지
)
data class Follow(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    // 팔로워: Follow를 요청한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    val follower: User,

    // 팔로우 대상: Follow 당한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followee_id", nullable = false)
    val followee: User,

    // 팔로우 관계 생성 시각
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
