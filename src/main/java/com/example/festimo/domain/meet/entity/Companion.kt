package com.example.festimo.domain.meet.entity

import com.example.festimo.domain.post.entity.Post
import com.example.festimo.domain.post.entity.PostCategory
import com.example.festimo.exception.InvalidTitleException
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "companion")
class Companion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val companionId: Long = 0,

    @Column(nullable = false)
    val leaderId: Long,

    @Column(nullable = false)
    val companionDate: LocalDateTime,

    // Post와 1:1 관계 설정
    @OneToOne
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    val post: Post,

    @Column(nullable = false, length = 255)
    var title: String = "동행",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CompanionStatus = CompanionStatus.ONGOING
) {
    // 기본 생성자
    constructor() : this(
        companionId = 0,
        leaderId = 0,
        companionDate = LocalDateTime.now(),
        post = Post(
            title = "",
            nickname = "",
            category = PostCategory.ETC,
            content = ""
        )
    )

    // 상태를 변경하는 메서드
    fun changeTitle(title: String) {
        if (title.isBlank()) {
            throw InvalidTitleException()
        }
        this.title = title
    }

    fun changeStatus(newStatus: CompanionStatus) {
        this.status = newStatus
    }
}

enum class CompanionStatus {
    ONGOING,
    COMPLETED
}