package com.example.festimo.domain.user.repository

import com.example.festimo.domain.user.domain.User
import com.example.festimo.domain.user.dto.UserNicknameProjection
import io.micrometer.common.lang.NonNull
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String?): User?

    fun existsByEmail(email: String?): Boolean

    fun existsByNickname(nickname: String?): Boolean

    fun findByRefreshToken(refreshToken: String?): User?

    fun findByProviderId(providerId: String?): User?

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.ratingAvg = :ratingAvg WHERE u.id = :userId")
    fun updateRatingAvg(@Param("userId") userId: Long?, @Param("ratingAvg") ratingAvg: Double?)


    @NonNull
    override fun findAll(@NonNull pageable: Pageable): Page<User?>

    @Query("SELECT u.id AS userId, u.nickname AS nickname FROM User u WHERE u.id IN :userIds")
    fun findNicknamesByUserIds(userIds: List<Long?>?): List<UserNicknameProjection?>?
}