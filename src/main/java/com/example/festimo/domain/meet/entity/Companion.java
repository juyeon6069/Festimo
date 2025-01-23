package com.example.festimo.domain.meet.entity;

import java.time.LocalDateTime;

import com.example.festimo.domain.post.entity.Post;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "companion")
public class Companion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companionId;

    @Column(nullable = false)
    private Long leaderId;

    @Column(nullable = false)
    private LocalDateTime companionDate;

    // Post와 1:1 관계 설정
    @OneToOne
    @JoinColumn(name = "post_id", nullable = false, unique = true) // 외래 키 설정
    private Post post;
}
