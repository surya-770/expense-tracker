package com.expensetracker.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.expensetracker.entity.InterestCalculation;

public interface InterestCalculationRepository extends JpaRepository<InterestCalculation,Long> {
    Page<InterestCalculation> findAllByUserId(Long userId,Pageable pageable);
    List<InterestCalculation> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<InterestCalculation> findByIdAndUserId(Long id,Long userId);
}
