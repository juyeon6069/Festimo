package com.example.festimo.domain.meet.entity;

import java.time.LocalDateTime;

import com.example.festimo.domain.user.domain.User;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "companion_member")
public class CompanionMember {

    @EmbeddedId
    private CompanionMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companion_id", insertable = false, updatable = false)
    private Companion companion; // 동행 정보와의 관계

    @ManyToOne
    @MapsId("userId") // EmbeddedId의 userId와 매핑
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;


    @Column(nullable = false)
    private LocalDateTime joinedDate;

}
