package com.example.festimo.domain.review.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDTO {
	private Long reviewerId;
	private Long revieweeId;
	private String content;
	private Long applicationId;
	private Long companyId2;

	@Min(1)
	@Max(5) // 평점은 1~5 사이의 정수만 허용
	private int rating;
}
