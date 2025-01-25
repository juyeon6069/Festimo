package com.example.festimo.domain.meet.repository

import com.example.festimo.domain.meet.entity.MemberResponse
import com.example.festimo.domain.meet.entity.CompanionMember
import com.example.festimo.domain.meet.entity.CompanionMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface CompanionMemberRepository : JpaRepository<CompanionMember, CompanionMemberId> {
    override fun existsById(companionMemberId: CompanionMemberId): Boolean

    @Query("SELECT DISTINCT cm FROM CompanionMember cm LEFT JOIN FETCH cm.companion c LEFT JOIN FETCH cm.user u LEFT JOIN FETCH c.post WHERE cm.id.userId = :userId")
    fun findByUserId(@Param("userId") userId: Long): List<CompanionMember>

    @Query("SELECT DISTINCT cm FROM CompanionMember cm LEFT JOIN FETCH cm.companion c LEFT JOIN FETCH cm.user u LEFT JOIN FETCH c.post WHERE cm.id.companionId = :companionId")
    fun findAllByCompanionId(@Param("companionId") companionId: Long): List<CompanionMember>

    fun deleteByCompanion_CompanionId(companionId: Long)

    @Query("""
    SELECT new com.example.festimo.domain.meet.entity.MemberResponse(
        cm.user.id,
        COALESCE(cm.user.userName, 'Unknown')
    )
    FROM CompanionMember cm
    WHERE cm.companion.companionId = :companionId
""")
    fun findMembersByCompanionId(companionId: Long): List<MemberResponse>



    @Query("""
    SELECT cm
    FROM CompanionMember cm
    JOIN FETCH cm.user u
    WHERE cm.companion.companionId = :companionId
""")
    fun findAllWithUserByCompanionId(companionId: Long): List<CompanionMember>

}