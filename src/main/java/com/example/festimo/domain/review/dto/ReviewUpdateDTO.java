package com.example.festimo.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewUpdateDTO {

	@Min(1)
	@Max(5)
	private int rating; // 평점

	@NotBlank
	private String content; // 수정할 내용

}
