package com.expensetracker.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Decimal logarithm/exponential helpers for fractional compounding periods. */
public final class InterestMath {
    private static final MathContext MC=new MathContext(34,RoundingMode.HALF_UP);
    private static final BigDecimal EPS=new BigDecimal("1E-36");
    private InterestMath(){}

    public static BigDecimal pow(BigDecimal base,BigDecimal exponent){
        if(base.signum()<=0)throw new IllegalArgumentException("Power base must be positive");
        if(exponent.signum()==0)return BigDecimal.ONE;
        return exp(log(base).multiply(exponent,MC));
    }

    private static BigDecimal log(BigDecimal value){
        BigDecimal x=value;int roots=8;for(int i=0;i<roots;i++)x=x.sqrt(MC);
        BigDecimal y=x.subtract(BigDecimal.ONE,MC).divide(x.add(BigDecimal.ONE,MC),MC),y2=y.multiply(y,MC);
        BigDecimal term=y,sum=y;
        for(int denominator=3;denominator<10000;denominator+=2){term=term.multiply(y2,MC);BigDecimal add=term.divide(BigDecimal.valueOf(denominator),MC);sum=sum.add(add,MC);if(add.abs().compareTo(EPS)<0)break;}
        return sum.multiply(BigDecimal.valueOf(2L<<roots),MC);
    }

    private static BigDecimal exp(BigDecimal value){
        int squares=0;BigDecimal x=value;while(x.abs().compareTo(new BigDecimal("0.5"))>0&&squares<2048){x=x.divide(BigDecimal.valueOf(2),MC);squares++;}
        BigDecimal sum=BigDecimal.ONE,term=BigDecimal.ONE;
        for(int i=1;i<10000;i++){term=term.multiply(x,MC).divide(BigDecimal.valueOf(i),MC);sum=sum.add(term,MC);if(term.abs().compareTo(EPS)<0)break;}
        for(int i=0;i<squares;i++)sum=sum.multiply(sum,MC);
        return sum;
    }
}
