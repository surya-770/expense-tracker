package com.expensetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.expensetracker.dto.SavingsDtos;
import com.expensetracker.entity.SavingsGoal;
import com.expensetracker.entity.User;
import com.expensetracker.repository.SavingsContributionRepository;
import com.expensetracker.repository.SavingsGoalRepository;
import com.expensetracker.repository.UserRepository;

class SavingsServiceTest {
    private SavingsGoalRepository goals;private SavingsContributionRepository contributions;private UserRepository users;private SavingsService service;
    @BeforeEach void setUp(){goals=org.mockito.Mockito.mock(SavingsGoalRepository.class);contributions=org.mockito.Mockito.mock(SavingsContributionRepository.class);users=org.mockito.Mockito.mock(UserRepository.class);service=new SavingsService(goals,contributions,users);User user=new User("Test","test@example.com","hash","USD");ReflectionTestUtils.setField(user,"id",9L);when(users.findByEmail("test@example.com")).thenReturn(Optional.of(user));}
    @Test void progressAndRemainingComeOnlyFromContributionLedger(){
        when(goals.save(any(SavingsGoal.class))).thenAnswer(call->{SavingsGoal goal=call.getArgument(0);ReflectionTestUtils.setField(goal,"id",4L);return goal;});
        when(contributions.totalForGoal(4L)).thenReturn(new BigDecimal("550.00"));when(contributions.findAllBySavingsGoalIdOrderByContributionDateDescIdDesc(4L)).thenReturn(List.of());
        SavingsDtos.GoalView goal=service.create("test@example.com",new SavingsDtos.GoalRequest("Emergency fund",new BigDecimal("500.00"),null,null));
        assertEquals(new BigDecimal("550.00"),goal.contributed());assertEquals(BigDecimal.ZERO,goal.remaining());assertEquals(new BigDecimal("110.00"),goal.progress());
    }
}
