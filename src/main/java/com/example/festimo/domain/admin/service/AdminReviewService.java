package com.example.festimo.domain.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.festimo.domain.admin.mapper.AdminReviewMapper;
import com.example.festimo.domain.review.domain.Review;
import com.example.festimo.domain.review.dto.ReviewResponseDTO;
import com.example.festimo.domain.review.repository.ReviewRepository;
import com.example.festimo.exception.CustomException;
import com.example.festimo.exception.ErrorCode;

@Service
public class AdminReviewService {

    private final ReviewRepository reviewRepository;

    public AdminReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /**
     * 모든 리뷰를 페이지네이션하여 조회합니다.
     *
     * @param pageable 페이지 정보
     * @return 페이지네이션된 리뷰 목록
     * @throws CustomException 리뷰가 존재하지 않을 경우 REVIEW_NOT_FOUND 예외 발생
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getAllReviews(Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findAll(pageable);

        if (reviewPage.isEmpty()) {
            throw new CustomException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return reviewPage.map(AdminReviewMapper.INSTANCE::toResponseDTO);
    }

    /**
     * 리뷰를 삭제합니다.
     *
     * @param reviewId 삭제할 리뷰의 ID
     * @throws CustomException 리뷰가 존재하지 않을 경우 REVIEW_NOT_FOUND 예외 발생
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        reviewRepository.delete(review);
    }
}