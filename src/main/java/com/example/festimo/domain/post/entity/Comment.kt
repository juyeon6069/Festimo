package com.example.festimo.domain.post.entity

import com.example.festimo.domain.post.BaseTimeEntity
import com.example.festimo.domain.user.domain.User
import jakarta.persistence.*

@Entity
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var comment: String,

    @Column(nullable = false)
    var nickname: String,

    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: Post,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @Column(nullable = false)
    var sequence: Int
) : BaseTimeEntity() {

    fun updateContent(newComment: String) {
        this.comment = newComment
    }
}