package com.expensetracker.repository;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.expensetracker.entity.PaymentMethod;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    Optional<Transaction> findByIdAndUserId(Long id,Long userId);
    boolean existsByCategory_Id(Long categoryId);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.user.id = :userId and t.transactionType = :type")
    BigDecimal sumByUserAndType(@Param("userId") Long userId,@Param("type") TransactionType type);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.user.id = :userId and t.transactionType = :type and t.transactionDate between :startDate and :endDate")
    BigDecimal sumByUserAndTypeForPeriod(@Param("userId") Long userId,@Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);

    @Query(value="select year(transaction_date), month(transaction_date), transaction_type, sum(amount) from transactions where user_id=:userId and transaction_date >= :fromDate group by year(transaction_date), month(transaction_date), transaction_type order by year(transaction_date), month(transaction_date)",nativeQuery=true)
    List<Object[]> monthlyTotals(@Param("userId") Long userId,@Param("fromDate") LocalDate fromDate);

    @Query("select t.category.name, t.category.color, coalesce(sum(t.amount),0) from Transaction t where t.user.id=:userId and t.transactionType=com.expensetracker.entity.TransactionType.EXPENSE and t.transactionDate between :startDate and :endDate group by t.category.name, t.category.color order by sum(t.amount) desc")
    List<Object[]> expenseDistribution(@Param("userId") Long userId,@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);

    @Query("select t.category.id, coalesce(sum(t.amount),0) from Transaction t where t.user.id=:userId and t.transactionType=com.expensetracker.entity.TransactionType.EXPENSE and t.transactionDate between :startDate and :endDate group by t.category.id")
    List<Object[]> expenseTotalsByCategory(@Param("userId") Long userId,@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);

    @Query("select t from Transaction t join fetch t.category where t.user.id = :userId " +
           "and (:type is null or t.transactionType = :type) " +
           "and (:categoryId is null or t.category.id = :categoryId) " +
           "and (:method is null or t.paymentMethod = :method) " +
           "and (:startDate is null or t.transactionDate >= :startDate) " +
           "and (:endDate is null or t.transactionDate <= :endDate) " +
           "and (:query = '' or lower(coalesce(t.description, '')) like lower(concat('%', :query, '%')) " +
           "or lower(coalesce(t.source, '')) like lower(concat('%', :query, '%')) " +
           "or lower(t.category.name) like lower(concat('%', :query, '%'))) ")
    Page<Transaction> search(@Param("userId") Long userId,@Param("type") TransactionType type,
           @Param("categoryId") Long categoryId,@Param("method") PaymentMethod method,
           @Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate,
           @Param("query") String query,Pageable pageable);
}
