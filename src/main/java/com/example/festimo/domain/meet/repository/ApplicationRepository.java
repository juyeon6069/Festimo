package com.example.festimo.domain.meet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.festimo.domain.meet.entity.Applications;

@Repository
public interface ApplicationRepository extends JpaRepository<Applications, Long> {
    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);

    //특정 활동의 신청 리스트 조회
    List<Applications> findByCompanyId(Long companyId);


}