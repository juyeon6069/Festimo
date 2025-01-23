package com.example.festimo.domain.admin.mapper;

import com.example.festimo.domain.admin.dto.AdminReviewDTO;
import com.example.festimo.domain.admin.entity.Reviews;
import java.time.LocalDate;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-23T16:13:06+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
public class AdminReviewMapperImpl implements AdminReviewMapper {

    @Override
    public AdminReviewDTO toDTO(Reviews reviews) {
        if ( reviews == null ) {
            return null;
        }

        Long reviewId = null;
        Long reviewerId = null;
        Long revieweeId = null;
        String rating = null;
        String content = null;
        LocalDate createdAt = null;
        LocalDate updatedAt = null;

        reviewId = reviews.getReviewId();
        reviewerId = reviews.getReviewerId();
        revieweeId = reviews.getRevieweeId();
        rating = reviews.getRating();
        content = reviews.getContent();
        createdAt = reviews.getCreatedAt();
        updatedAt = reviews.getUpdatedAt();

        AdminReviewDTO adminReviewDTO = new AdminReviewDTO( reviewId, reviewerId, revieweeId, rating, content, createdAt, updatedAt );

        return adminReviewDTO;
    }
}
