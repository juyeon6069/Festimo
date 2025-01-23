package com.example.festimo.domain.meet.dto;

import com.example.festimo.domain.meet.entity.Applications;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class LeaderApplicationResponse {
    private Long applicationId;            // 신청 ID
    private Long userId;                   // 신청한 사용자 ID
    private Long companionId;              // 동행 ID (추가됨)
    private String nickname;               // 신청한 사용자의 닉네임
    private Applications.Status status;    // 신청 상태 (PENDING, ACCEPTED, REJECTED)
    private LocalDateTime appliedDate;     // 신청 날짜
}