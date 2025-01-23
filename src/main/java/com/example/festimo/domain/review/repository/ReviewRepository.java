package com.example.festimo.domain.review.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.festimo.domain.review.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	List<Review> findByRevieweeId(Long revieweeId);
	List<Review> findByReviewerId(Long reviewerId);

	// 페이징 및 정렬
	Page<Review> findByRevieweeId(Long revieweeId, Pageable pageable);
	Page<Review> findByReviewerId(Long reviewerId, Pageable pageable);

	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.revieweeId = :userId")
	Double findAverageRatingByRevieweeId(@Param("userId") Long userId);


}
