package com.example.festimo.domain.post.entity

import com.example.festimo.domain.post.BaseTimeEntity
import com.example.festimo.domain.user.domain.User
import jakarta.persistence.*

@Entity
@Table(indexes = [
    Index(name = "idx_post_created_at", columnList = "createdAt"),
    Index(name = "idx_post_likes", columnList = "likes")
])
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    var id: Long? = null,

    @Column(nullable = false, length = 50)
    var title: String,

    @Column(nullable = false, length = 15)
    var nickname: String,

    var avatar: String? = null,
    var mail: String? = null,
    var password: String? = null,

    @Enumerated(EnumType.STRING)
    var category: PostCategory,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column
    var imagePath: String? = null,

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    var views: Int = 0,

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    var replies: Int = 0,

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    var likes: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    @OrderBy("sequence asc")
    var comments: MutableList<Comment> = mutableListOf(),

    @ManyToMany
    @JoinTable(
        name = "post_likes",
        joinColumns = [JoinColumn(name = "post_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var likedByUsers: MutableSet<User> = HashSet(),

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "post_tags", joinColumns = [JoinColumn(name = "post_id")])
    @Column(name = "tag")
    var tags: MutableSet<String> = HashSet()
) : BaseTimeEntity() {

    fun toggleLike(user: User) {
        if (likedByUsers.contains(user)) {
            likedByUsers.remove(user)
            likes--
        } else {
            likedByUsers.add(user)
            likes++
        }
    }
}