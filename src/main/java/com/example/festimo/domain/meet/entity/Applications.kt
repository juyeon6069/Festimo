package com.example.festimo.domain.meet.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Applications(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val applicationId: Long = 0,

    @Column(nullable = false)
    var userId: Long = 0,

    @Column(nullable = false)
    var companionId: Long = 0,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: Status = Status.PENDING,

    @Column(nullable = false)
    val appliedDate: LocalDateTime = LocalDateTime.now()
) {
    // 기본 생성자
    protected constructor() : this(0, 0, 0)

    // 보조 생성자
    constructor(userId: Long, companionId: Long) : this(
        userId = userId,
        companionId = companionId,
        applicationId = 0
    )

    enum class Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}