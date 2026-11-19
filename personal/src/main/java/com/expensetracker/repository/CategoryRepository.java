package com.expensetracker.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.expensetracker.entity.Category;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    @Query("select c from Category c where c.userId is null or c.userId = :userId order by c.type, c.name")
    List<Category> findAvailableForUser(@Param("userId") Long userId);
    Optional<Category> findByIdAndUserId(Long id, Long userId);
    boolean existsByNameIgnoreCaseAndTypeAndUserId(String name,String type,Long userId);
}
