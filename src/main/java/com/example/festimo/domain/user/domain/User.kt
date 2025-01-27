package com.example.festimo.domain.user.domain

import jakarta.persistence.*
import lombok.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var id: Long? = null,

    @Column(name = "user_name", nullable = false, length = 50)
    var userName: String? = null,

    @Column(name = "nickname", nullable = false, unique = true, length = 255)
    var nickname: String? = null,

    @Column
    @Builder.Default
    var avatar: String = "default-avatar.png",

    @Column(name = "email", nullable = false, unique = true, length = 255)
    var email: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    var gender: Gender? = null,

    @Column(name = "password", nullable = true, length = 255)
    var password: String? = null, // 소셜 로그인 사용자는 비밀번호가 없을 수 있음

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: Role? = null,

    @Column(name = "refresh_token", length = 512)
    var refreshToken: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    var provider: Provider? = null,

    @Column(name = "provider_id", length = 255, unique = true)
    var providerId: String? = null,

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    var createdDate: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "modified_date")
    private var modifiedDate: LocalDateTime? = null,

    @Column(name = "rating_avg", nullable = false)
    @Builder.Default
    var ratingAvg: Float? = 0.0f
){

    // Enum Classes
    enum class Gender {
        M, F
    }

    enum class Role {
        ADMIN, USER
    }

    enum class Provider {
        KAKAO, NAVER, LOCAL
    }
}


