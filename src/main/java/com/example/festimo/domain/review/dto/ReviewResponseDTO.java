package com.example.festimo.domain.review.dto;

import java.time.LocalDateTime;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewResponseDTO {
	private Long reviewId;
	private Long reviewerId;
	private Long revieweeId;
	private int rating;
	private String content;
	private LocalDateTime createdAt;
}
