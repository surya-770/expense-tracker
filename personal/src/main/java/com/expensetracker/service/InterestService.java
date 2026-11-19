package com.expensetracker.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.expensetracker.dto.InterestDtos;
import com.expensetracker.entity.CompoundingFrequency;
import com.expensetracker.entity.DurationUnit;
import com.expensetracker.entity.InterestCalculation;
import com.expensetracker.entity.InterestType;
import com.expensetracker.entity.User;
import com.expensetracker.repository.InterestCalculationRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.util.InterestMath;

@Service
public class InterestService {
    private static final MathContext MC=new MathContext(34,RoundingMode.HALF_UP);private static final int MONEY_SCALE=2;
    private final InterestCalculationRepository calculations;private final UserRepository users;
    public InterestService(InterestCalculationRepository calculations,UserRepository users){this.calculations=calculations;this.users=users;}

    public InterestDtos.Result calculate(InterestType type,InterestDtos.Request request){return result(null,type,request,calculateValues(type,request),null);}
    @Transactional
    public InterestDtos.Result save(String email,InterestType type,InterestDtos.Request request){User user=account(email);Values values=calculateValues(type,request);InterestCalculation saved=calculations.save(new InterestCalculation(user,type,request.principal().setScale(2,RoundingMode.HALF_UP),request.annualRate(),request.duration(),request.durationUnit(),type==InterestType.COMPOUND?request.compoundingFrequency():null,values.interest(),values.amount()));return result(saved.getId(),type,request,values,saved.getCreatedAt());}
    @Transactional(readOnly=true)
    public List<InterestDtos.Result> history(String email,int page,int size){if(page<0||size<1||size>100)throw new IllegalArgumentException("Page size must be between 1 and 100");return calculations.findAllByUserId(account(email).getId(),PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt"))).map(this::fromEntity).getContent();}
    @Transactional
    public void delete(String email,long id){InterestCalculation item=calculations.findByIdAndUserId(id,account(email).getId()).orElseThrow(()->new IllegalArgumentException("Calculation not found"));calculations.delete(item);}
    @Transactional(readOnly=true)
    public String exportCsv(String email){List<InterestCalculation> rows=calculations.findAllByUserIdOrderByCreatedAtDesc(account(email).getId());StringBuilder csv=new StringBuilder("\uFEFFsaved_at,type,principal,annual_rate,duration,duration_unit,compounding_frequency,interest_amount,final_amount\r\n");for(var row:rows){csv.append(row.getCreatedAt()).append(',').append(row.getCalculationType()).append(',').append(row.getPrincipal().toPlainString()).append(',').append(row.getAnnualRate().toPlainString()).append(',').append(row.getDuration().toPlainString()).append(',').append(row.getDurationUnit()).append(',').append(row.getCompoundingFrequency()==null?"":row.getCompoundingFrequency()).append(',').append(row.getInterestAmount().toPlainString()).append(',').append(row.getFinalAmount().toPlainString()).append("\r\n");}return csv.toString();}

    private Values calculateValues(InterestType type,InterestDtos.Request request){
        BigDecimal years=request.durationUnit()==DurationUnit.MONTHS?request.duration().divide(BigDecimal.valueOf(12),MC):request.duration();
        if(years.compareTo(BigDecimal.valueOf(100))>0)throw new IllegalArgumentException("Maximum duration is 100 years");
        BigDecimal principal=request.principal().setScale(2,RoundingMode.HALF_UP);BigDecimal interest,amount;
        if(type==InterestType.SIMPLE){interest=principal.multiply(request.annualRate(),MC).multiply(years,MC).divide(BigDecimal.valueOf(100),MC).setScale(MONEY_SCALE,RoundingMode.HALF_UP);amount=principal.add(interest).setScale(MONEY_SCALE,RoundingMode.HALF_UP);}
        else{
            if(request.compoundingFrequency()==null)throw new IllegalArgumentException("Compounding frequency is required");int n=periods(request.compoundingFrequency());
            BigDecimal periodic=request.annualRate().divide(BigDecimal.valueOf(n*100L),MC);BigDecimal base=BigDecimal.ONE.add(periodic,MC);BigDecimal exponent=years.multiply(BigDecimal.valueOf(n),MC);
            amount=principal.multiply(InterestMath.pow(base,exponent),MC).setScale(MONEY_SCALE,RoundingMode.HALF_UP);interest=amount.subtract(principal).setScale(MONEY_SCALE,RoundingMode.HALF_UP);
        }
        if(amount.precision()-amount.scale()>17)throw new IllegalArgumentException("Calculated amount exceeds the supported maximum");
        return new Values(interest,amount);
    }
    private InterestDtos.Result result(Long id,InterestType type,InterestDtos.Request request,Values values,java.time.Instant created){
        BigDecimal years=request.durationUnit()==DurationUnit.MONTHS?request.duration().divide(BigDecimal.valueOf(12),MC):request.duration();List<InterestDtos.GrowthPoint> growth=new ArrayList<>();int n=type==InterestType.COMPOUND?periods(request.compoundingFrequency()):1;
        for(int i=0;i<=12;i++){BigDecimal t=years.multiply(BigDecimal.valueOf(i),MC).divide(BigDecimal.valueOf(12),MC);BigDecimal value;if(type==InterestType.SIMPLE){value=request.principal().multiply(request.annualRate(),MC).multiply(t,MC).divide(BigDecimal.valueOf(100),MC).add(request.principal(),MC);}else{BigDecimal base=BigDecimal.ONE.add(request.annualRate().divide(BigDecimal.valueOf(n*100L),MC),MC);value=request.principal().multiply(InterestMath.pow(base,t.multiply(BigDecimal.valueOf(n),MC)),MC);}growth.add(new InterestDtos.GrowthPoint(t.setScale(4,RoundingMode.HALF_UP),value.setScale(2,RoundingMode.HALF_UP)));}
        return new InterestDtos.Result(id,type,request.principal().setScale(2,RoundingMode.HALF_UP),request.annualRate(),request.duration(),request.durationUnit(),request.compoundingFrequency(),values.interest(),values.amount(),growth,created);
    }
    private InterestDtos.Result fromEntity(InterestCalculation item){var request=new InterestDtos.Request(item.getPrincipal(),item.getAnnualRate(),item.getDuration(),item.getDurationUnit(),item.getCompoundingFrequency());return result(item.getId(),item.getCalculationType(),request,new Values(item.getInterestAmount(),item.getFinalAmount()),item.getCreatedAt());}
    private static int periods(CompoundingFrequency frequency){return switch(frequency){case YEARLY->1;case HALF_YEARLY->2;case QUARTERLY->4;case MONTHLY->12;};}
    private User account(String email){return users.findByEmail(email.toLowerCase()).orElseThrow(()->new IllegalStateException("Authenticated account was not found"));}
    private record Values(BigDecimal interest,BigDecimal amount){}
}
