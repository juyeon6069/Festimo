package com.example.festimo.domain.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.festimo.domain.admin.service.AdminReviewService;
import com.example.festimo.domain.review.dto.ReviewResponseDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@Tag(name = "관리자 API", description = "관리자가 리뷰를 관리하는 API")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    public AdminReviewController(AdminReviewService adminReviewService) {
        this.adminReviewService = adminReviewService;
    }

    /**
     * 관리자의 리뷰 조회 (최신순)
     *
     * @param page 조회할 페이지 번호 (기본값: 0)
     * @param size 한 페이지에 표시할 리뷰 개수 (기본값: 10)
     * @return 페이지네이션된 최신순 리뷰 목록
     */
    @GetMapping()
    @Operation(summary = "관리자의 리뷰 조회 (최신순)", description = "모든 리뷰를 최신순으로 조회")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewResponseDTO> reviews = adminReviewService.getAllReviews(pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 관리자의 리뷰 삭제
     *
     * @param reviewId 삭제할 리뷰의 ID
     * @return 성공적인 삭제 응답
     */
    @DeleteMapping("/{reviewId}")
    @Operation(summary = "관리자의 리뷰 삭제", description = "특정 리뷰 삭제")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        adminReviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}