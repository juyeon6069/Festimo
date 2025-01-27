package com.example.festimo.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    @Column(name = "nickname", nullable = false, unique = true, length = 255)
    private String nickname;

    @Column
    @Builder.Default
    private String avatar = "default-avatar.png";

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "password", nullable = true, length = 255)
    private String password; // 소셜 로그인 사용자는 비밀번호가 없을 수 있음

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private Provider provider;

    @Column(name = "provider_id", length = 255, unique = true)
    private String providerId;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "rating_avg", nullable = false)
    @Builder.Default
    private Float ratingAvg = 0.0f;

    // Enum Classes
    public enum Gender {
        M, F
    }

    public enum Role {
        ADMIN, USER
    }

    public enum Provider {
        KAKAO, NAVER, LOCAL
    }

}

