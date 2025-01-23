package com.example.festimo.domain.meet.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Applications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @Column( nullable = false)
    private Long userId;

    @Column( nullable = false)
    private Long companionId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING; // 기본값은 'PENDING'

    @Column(nullable = false)
    private LocalDateTime appliedDate = LocalDateTime.now();

    public Applications(Long userId, Long companionId) {
        this.userId = userId;
        this.companionId = companionId;
    }

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }

}
