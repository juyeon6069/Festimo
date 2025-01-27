package com.example.festimo.domain.meet.entity

import com.example.festimo.domain.post.entity.Post
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
    val companionDate: LocalDateTime = LocalDateTime.now(),

    //@OneToOne
    //@JoinColumn(name = "post_id", nullable = false)
    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post
) {
    constructor() : this(0, 0, LocalDateTime.now(), Post())
}