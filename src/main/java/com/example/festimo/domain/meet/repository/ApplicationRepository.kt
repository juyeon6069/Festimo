package com.example.festimo.domain.meet.repository

import com.example.festimo.domain.meet.entity.Applications
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository : JpaRepository<Applications, Long> {
    fun existsByUserIdAndCompanionId(userId: Long, companionId: Long): Boolean

    fun findByCompanionIdAndStatus(companionId: Long, status: Applications.Status): List<Applications>?

}
