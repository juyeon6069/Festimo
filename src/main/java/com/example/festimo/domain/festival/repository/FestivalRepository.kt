package com.example.festimo.domain.festival.repository

import com.example.festimo.domain.festival.domain.Festival
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface FestivalRepository : JpaRepository<Festival, String> {

    fun findByTitleContainingIgnoreCase(keyword: String?, pageable: Pageable?): Page<Festival>

    fun findByAddressContainingIgnoreCase(region: String?, pageable: Pageable?): Page<Festival>

    @Query("SELECT f FROM Festival f WHERE " +
            "f.startDate <= :endDate AND f.endDate >= :startDate")
    fun findByMonth(
        @Param("startDate") startDate: LocalDate?,
        @Param("endDate") endDate: LocalDate?,
        pageable: Pageable?
    ): Page<Festival>
}
