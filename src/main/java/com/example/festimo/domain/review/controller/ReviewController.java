package com.example.festimo.domain.review.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.festimo.domain.review.dto.ReviewRequestDTO;
import com.example.festimo.domain.review.dto.ReviewResponseDTO;
import com.example.festimo.domain.review.dto.ReviewUpdateDTO;
import com.example.festimo.domain.review.service.ReviewService;
import com.example.festimo.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "리뷰 API", description = "리뷰 정보를 관리하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

	private final ReviewService reviewService;
	private final UserService userService;

	@Operation(summary = "리뷰 작성")
	@PostMapping
	public ResponseEntity<String> createReview(@RequestBody @Valid ReviewRequestDTO requestDTO) {
		String message = reviewService.createReview(requestDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(message);
	}

	@Operation(summary = "리뷰 조회")
	@GetMapping("/reviewee/{revieweeId}")
	public ResponseEntity<List<ReviewResponseDTO>> getReviewsForUser(@PathVariable Long revieweeId) {
		List<ReviewResponseDTO> reviews = reviewService.getReviewsForUser(revieweeId);
		return ResponseEntity.ok(reviews);
	}

	// 페이징 및 정렬
	@GetMapping("/reviewee/{revieweeId}/paged")
	public ResponseEntity<Page<ReviewResponseDTO>> getPagedReviews(
		@PathVariable Long revieweeId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "desc") String sortDir
	) {
		Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<ReviewResponseDTO> reviews = reviewService.getPagedReviewsForUser(revieweeId, pageable);
		return ResponseEntity.ok(reviews);
	}

	@Operation(summary = "내가 받은 리뷰 페이징 조회 - 마이페이지")
	@GetMapping("/reviewee/mypage/paged")
	public ResponseEntity<Page<ReviewResponseDTO>> getPagedReviews(
		Authentication authentication,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "desc") String sortDir
	) {
		String email = authentication.getName(); // JWT에서 이메일 추출
		Long revieweeId = userService.getUserIdByEmail(email); // 이메일로 사용자 ID 조회

		Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<ReviewResponseDTO> reviews = reviewService.getPagedReviewsForUser(revieweeId, pageable);
		return ResponseEntity.ok(reviews);
	}


	@Operation(summary = "내가 쓴 리뷰 조회")
	@GetMapping("/reviewer/{reviewerId}")
	public ResponseEntity<List<ReviewResponseDTO>> getReviewsByReviewer(@PathVariable Long reviewerId) {
		List<ReviewResponseDTO> reviews = reviewService.getReviewsByReviewer(reviewerId);
		return ResponseEntity.ok(reviews);
	}

	@Operation(summary = "내가 쓴 리뷰 페이징 조회 - 마이페이지")
	@GetMapping("/reviewer/mypage/paged")
	public ResponseEntity<Page<ReviewResponseDTO>> getPagedReviewsForAuthenticatedUser(
		Authentication authentication,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "5") int size,
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "desc") String sortDir
	) {
		String email = authentication.getName(); // JWT에서 이메일 추출
		Long reviewerId = userService.getUserIdByEmail(email); // 이메일로 사용자 ID 조회

		Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<ReviewResponseDTO> reviews = reviewService.getPagedReviewsByReviewer(reviewerId, pageable);

		return ResponseEntity.ok(reviews);
	}




	@Operation(summary = "리뷰 삭제")
	@DeleteMapping("/{reviewId}")
	public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
		return ResponseEntity.ok(reviewService.deleteReview(reviewId));
	}

	@Operation(summary = "리뷰 수정")
	@PutMapping("/{reviewId}")
	public ResponseEntity<String> updateReview(
		@PathVariable Long reviewId,
		@RequestBody @Valid ReviewUpdateDTO updateDTO
	) {
		return ResponseEntity.ok(reviewService.updateReview(reviewId, updateDTO));
	}

}