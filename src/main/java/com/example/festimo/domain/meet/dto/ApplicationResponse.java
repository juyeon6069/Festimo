package com.example.festimo.domain.meet.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.example.festimo.domain.meet.entity.Applications;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long applicationId;
    private Long userId;
    private Long companionId;
    private Applications.Status status;
    private LocalDateTime appliedDate;
}