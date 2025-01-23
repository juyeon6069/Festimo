package com.example.festimo.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AdminReviewDTO {

    private Long reviewId;

    private Long reviewerId;

    private Long revieweeId;

    private String rating;

    private String content;

    private LocalDate createdAt;

    private LocalDate updatedAt;

}
