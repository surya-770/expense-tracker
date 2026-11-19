package com.expensetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.expensetracker.dto.InterestDtos;
import com.expensetracker.entity.CompoundingFrequency;
import com.expensetracker.entity.DurationUnit;
import com.expensetracker.entity.InterestType;
import com.expensetracker.repository.InterestCalculationRepository;
import com.expensetracker.repository.UserRepository;

class InterestServiceTest {
    private InterestService service;
    @BeforeEach void setUp(){service=new InterestService(org.mockito.Mockito.mock(InterestCalculationRepository.class),org.mockito.Mockito.mock(UserRepository.class));}
    @Test void calculatesSimpleInterestAndFinalAmount(){
        var result=service.calculate(InterestType.SIMPLE,new InterestDtos.Request(new BigDecimal("1000.00"),new BigDecimal("5.0000"),new BigDecimal("2.00"),DurationUnit.YEARS,null));
        assertEquals(new BigDecimal("100.00"),result.interestAmount());assertEquals(new BigDecimal("1100.00"),result.finalAmount());assertEquals(new BigDecimal("1000.00"),result.growth().get(0).amount());
    }
    @Test void calculatesMonthlyCompoundInterestWithDecimalMath(){
        var result=service.calculate(InterestType.COMPOUND,new InterestDtos.Request(new BigDecimal("1000.00"),new BigDecimal("10.0000"),new BigDecimal("1.00"),DurationUnit.YEARS,CompoundingFrequency.MONTHLY));
        assertEquals(new BigDecimal("104.71"),result.interestAmount());assertEquals(new BigDecimal("1104.71"),result.finalAmount());
    }
    @Test void supportsFractionalCompoundingPeriods(){
        var result=service.calculate(InterestType.COMPOUND,new InterestDtos.Request(new BigDecimal("1000.00"),new BigDecimal("10.0000"),new BigDecimal("6.00"),DurationUnit.MONTHS,CompoundingFrequency.YEARLY));
        assertEquals(new BigDecimal("48.81"),result.interestAmount());assertEquals(new BigDecimal("1048.81"),result.finalAmount());
    }
}
