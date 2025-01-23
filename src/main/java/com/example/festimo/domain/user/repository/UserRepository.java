package com.example.festimo.domain.user.repository;

import com.example.festimo.domain.user.domain.User;
import com.example.festimo.domain.user.dto.UserNicknameProjection;
import io.micrometer.common.lang.NonNull;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByRefreshToken(String refreshToken);

    Optional<User> findByProviderId(String providerId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.ratingAvg = :ratingAvg WHERE u.id = :userId")
    void updateRatingAvg(@Param("userId") Long userId, @Param("ratingAvg") Double ratingAvg);


    @NonNull
    Page<User> findAll(@NonNull Pageable pageable);

    @Query("SELECT u.id AS userId, u.nickname AS nickname FROM User u WHERE u.id IN :userIds")
    List<UserNicknameProjection> findNicknamesByUserIds(List<Long> userIds);
}