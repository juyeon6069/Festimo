package com.example.festimo.domain.review.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.festimo.domain.review.domain.Review;
import com.example.festimo.domain.review.dto.ReviewRequestDTO;
import com.example.festimo.domain.review.dto.ReviewResponseDTO;
import com.example.festimo.domain.review.dto.ReviewUpdateDTO;
import com.example.festimo.domain.review.repository.ReviewRepository;
import com.example.festimo.domain.user.service.UserService;
import com.example.festimo.exception.CustomException;
import com.example.festimo.exception.ErrorCode;

import jakarta.transaction.Transactional;

@Service
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final UserService userService;
	private final ModelMapper modelMapper;

	public ReviewService(ReviewRepository reviewRepository, ModelMapper modelMapper, UserService userService) {
		this.reviewRepository = reviewRepository;
		this.modelMapper = modelMapper;
		this.userService = userService;

	}


	// public String createReview(ReviewRequestDTO requestDTO) {
	// 	try {
	// 		Review review = modelMapper.map(requestDTO, Review.class);
	// 		reviewRepository.save(review);
	// 		// 평점 평균 업데이트
	// 		userService.updateUserRatingAvg(requestDTO.getRevieweeId());
	// 		return "Review created successfully.";
	// 	} catch (Exception e) {
	// 		throw new RuntimeException("Failed to create review. Reason: " + e.getMessage());
	// 	}
	// }

	@Transactional
	public String createReview(ReviewRequestDTO requestDTO) {
		try {
			// ReviewRequestDTO를 Review로 직접 변환
			Review review = new Review();
			review.setReviewerId(requestDTO.getReviewerId());
			review.setRevieweeId(requestDTO.getRevieweeId());
			review.setContent(requestDTO.getContent());
			review.setRating(requestDTO.getRating());
			review.setApplicationId(requestDTO.getApplicationId());
			review.setCompanyId2(requestDTO.getCompanyId2());
			review.setCreatedAt(LocalDateTime.now());
			review.setUpdatedAt(LocalDateTime.now());

			// Review 저장
			reviewRepository.save(review);

			// 평점 평균 업데이트
			userService.updateUserRatingAvg(requestDTO.getRevieweeId());

			return "Review created successfully.";
		} catch (Exception e) {
			throw new RuntimeException("Failed to create review. Reason: " + e.getMessage());
		}
	}



	public List<ReviewResponseDTO> getReviewsForUser(Long revieweeId) {
		List<Review> reviews = reviewRepository.findByRevieweeId(revieweeId);
		if (reviews.isEmpty()) {
			throw new CustomException(ErrorCode.REVIEW_NOT_FOUND);
		}
		return reviews.stream()
			.map(review -> modelMapper.map(review, ReviewResponseDTO.class))
			.collect(Collectors.toList());
	}

	// 페이징 및 정렬
	public Page<ReviewResponseDTO> getPagedReviewsForUser(Long revieweeId, Pageable pageable) {
		Page<Review> reviewPage = reviewRepository.findByRevieweeId(revieweeId, pageable);
		if (reviewPage.isEmpty()) {
			throw new CustomException(ErrorCode.REVIEW_NOT_FOUND);
		}
		return reviewPage.map(review -> modelMapper.map(review, ReviewResponseDTO.class));
	}

	// 작성자 ID로 리뷰 리스트 조회
	public List<ReviewResponseDTO> getReviewsByReviewer(Long reviewerId) {
		List<Review> reviews = reviewRepository.findByReviewerId(reviewerId);
		if (reviews.isEmpty()) {
			throw new CustomException(ErrorCode.REVIEW_NOT_FOUND);
		}
		return reviews.stream()
			.map(review -> modelMapper.map(review, ReviewResponseDTO.class))
			.collect(Collectors.toList());
	}

	// 작성자 ID로 페이징 리뷰 조회
	public Page<ReviewResponseDTO> getPagedReviewsByReviewer(Long reviewerId, Pageable pageable) {
		Page<Review> reviewPage = reviewRepository.findByReviewerId(reviewerId, pageable);
		if (reviewPage.isEmpty()) {
			throw new CustomException(ErrorCode.REVIEW_NOT_FOUND);
		}
		return reviewPage.map(review -> modelMapper.map(review, ReviewResponseDTO.class));
	}

	// 리뷰 삭제
	@Transactional
	public String deleteReview(Long reviewId) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		Long revieweeId = review.getRevieweeId();
		reviewRepository.delete(review);

		// 평점 평균 업데이트
		userService.updateUserRatingAvg(revieweeId);
		return "Review deleted successfully.";
	}

	// 리뷰 수정
	@Transactional
	public String updateReview(Long reviewId, ReviewUpdateDTO updateDTO) {
		// 리뷰 조회
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		// 수정 가능한 필드 업데이트
		review.setRating(updateDTO.getRating());
		review.setContent(updateDTO.getContent());
		review.setUpdatedAt(LocalDateTime.now());

		// 저장
		reviewRepository.save(review);

		return "Review updated successfully.";
	}

}
