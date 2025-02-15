package com.example.festimo.domain.festival.repository;

import com.example.festimo.domain.festival.domain.Festival;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, String> {
    Page<Festival> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Festival> findByAddressContainingIgnoreCase(String region, Pageable pageable);

    @Query("SELECT f FROM Festival f WHERE " +
            "f.startDate <= :endDate AND f.endDate >= :startDate")
    Page<Festival> findByMonth(@Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate,
                               Pageable pageable);
}
