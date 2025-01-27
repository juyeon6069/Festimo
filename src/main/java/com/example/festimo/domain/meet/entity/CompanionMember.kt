package com.example.festimo.domain.meet.entity

import com.example.festimo.domain.user.domain.User
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "companion_member")
class CompanionMember(
    @EmbeddedId
    var id: CompanionMemberId,

    @ManyToOne(fetch = FetchType.EAGER) // 기존 LAZY -> EAGER로 변경
    @MapsId("companionId")
    @JoinColumn(name = "companion_id")
    var companion: Companion,

    @ManyToOne(fetch = FetchType.EAGER) // 기존 LAZY -> EAGER로 변경
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    var user: User,

    @Column(nullable = false)
    var joinedDate: LocalDateTime
) {
    constructor() : this(CompanionMemberId(0, 0), Companion(), User(), LocalDateTime.now())
}