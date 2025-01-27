package com.example.festimo.domain.meet.repository

import com.example.festimo.domain.meet.entity.Companion
import com.example.festimo.domain.meet.dto.MemberResponse
import com.example.festimo.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface CompanionRepository : JpaRepository<Companion, Long> {
    fun findByLeaderId(leaderId: Long): List<Companion>
    fun findByPost(post: Post): Optional<Companion>

    @Query("SELECT c.leaderId FROM Companion c WHERE c.companionId = :companionId")
    fun findLeaderIdByCompanyId(companionId: Long): Optional<Long>

    @Query("SELECT c.companionId FROM Companion c WHERE c.post.id = :postId")
    fun findCompanionIdByPostId(postId: Long): Optional<Long>

    @Query("""
    SELECT new com.example.festimo.domain.meet.dto.MemberResponse(
        u.id,
        u.userName
    )
    FROM User u
    WHERE u.id = :leaderId
""")
    fun findLeaderById(leaderId: Long): MemberResponse


}