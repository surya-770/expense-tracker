package com.expensetracker.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.expensetracker.dto.FinanceDtos;
import com.expensetracker.entity.User;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;

class TransactionServiceTest {
    private TransactionRepository transactions;private CategoryRepository categories;private UserRepository users;private TransactionService service;
    @BeforeEach void setUp(){transactions=org.mockito.Mockito.mock(TransactionRepository.class);categories=org.mockito.Mockito.mock(CategoryRepository.class);users=org.mockito.Mockito.mock(UserRepository.class);service=new TransactionService(transactions,categories,users);User user=new User("One","one@example.com","hash","USD");ReflectionTestUtils.setField(user,"id",1L);when(users.findByEmail("one@example.com")).thenReturn(Optional.of(user));}
    @Test void anotherUsersTransactionCannotBeRead(){when(transactions.findByIdAndUserId(99L,1L)).thenReturn(Optional.empty());assertThrows(IllegalArgumentException.class,()->service.get("one@example.com",99L));}
    @Test void foreignOrMissingCategoryCannotBeUsedForExpense(){when(categories.findAvailableForUser(1L)).thenReturn(List.of());var request=new FinanceDtos.TransactionRequest(com.expensetracker.entity.TransactionType.EXPENSE,new java.math.BigDecimal("12.00"),java.time.LocalDate.of(2026,11,5),99L,null,com.expensetracker.entity.PaymentMethod.UPI,"test");assertThrows(IllegalArgumentException.class,()->service.create("one@example.com",request));verify(transactions,never()).save(org.mockito.ArgumentMatchers.any());}
}
