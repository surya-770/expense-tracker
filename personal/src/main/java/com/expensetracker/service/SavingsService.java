package com.expensetracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.expensetracker.dto.SavingsDtos;
import com.expensetracker.entity.GoalStatus;
import com.expensetracker.entity.SavingsContribution;
import com.expensetracker.entity.SavingsGoal;
import com.expensetracker.entity.User;
import com.expensetracker.repository.SavingsContributionRepository;
import com.expensetracker.repository.SavingsGoalRepository;
import com.expensetracker.repository.UserRepository;

@Service
public class SavingsService {
    private final SavingsGoalRepository goals;private final SavingsContributionRepository contributions;private final UserRepository users;
    public SavingsService(SavingsGoalRepository goals,SavingsContributionRepository contributions,UserRepository users){this.goals=goals;this.contributions=contributions;this.users=users;}
    @Transactional(readOnly=true) public List<SavingsDtos.GoalView> list(String email){User user=account(email);return goals.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::view).toList();}
    @Transactional public SavingsDtos.GoalView create(String email,SavingsDtos.GoalRequest request){User user=account(email);return view(goals.save(new SavingsGoal(user,request.name().trim(),request.targetAmount(),request.targetDate(),clean(request.description()))));}
    @Transactional public SavingsDtos.GoalView update(String email,long id,SavingsDtos.GoalRequest request){SavingsGoal goal=owned(email,id);goal.update(request.name().trim(),request.targetAmount(),request.targetDate(),clean(request.description()));return view(goals.save(goal));}
    @Transactional public SavingsDtos.GoalView setStatus(String email,long id,GoalStatus status){SavingsGoal goal=owned(email,id);goal.setStatus(status);return view(goals.save(goal));}
    @Transactional public void delete(String email,long id){goals.delete(owned(email,id));}
    @Transactional(readOnly=true) public SavingsDtos.GoalView get(String email,long id){return view(owned(email,id));}
    @Transactional public SavingsDtos.ContributionView add(String email,long goalId,SavingsDtos.ContributionRequest request){SavingsGoal goal=owned(email,goalId);return response(contributions.save(new SavingsContribution(goal,request.amount(),request.date(),clean(request.description()))));}
    @Transactional(readOnly=true) public List<SavingsDtos.ContributionView> history(String email,long goalId){SavingsGoal goal=owned(email,goalId);return contributions.findAllBySavingsGoalIdOrderByContributionDateDescIdDesc(goal.getId()).stream().map(SavingsService::response).toList();}
    @Transactional public SavingsDtos.ContributionView updateContribution(String email,long goalId,long contributionId,SavingsDtos.ContributionRequest request){SavingsGoal goal=owned(email,goalId);SavingsContribution contribution=contributions.findByIdAndSavingsGoalId(contributionId,goal.getId()).orElseThrow(()->new IllegalArgumentException("Contribution not found"));contribution.update(request.amount(),request.date(),clean(request.description()));return response(contributions.save(contribution));}
    @Transactional public void deleteContribution(String email,long goalId,long contributionId){SavingsGoal goal=owned(email,goalId);SavingsContribution contribution=contributions.findByIdAndSavingsGoalId(contributionId,goal.getId()).orElseThrow(()->new IllegalArgumentException("Contribution not found"));contributions.delete(contribution);}
    private SavingsDtos.GoalView view(SavingsGoal goal){BigDecimal contributed=contributions.totalForGoal(goal.getId());BigDecimal remaining=goal.getTargetAmount().subtract(contributed).max(BigDecimal.ZERO);BigDecimal progress=contributed.multiply(BigDecimal.valueOf(100)).divide(goal.getTargetAmount(),2,RoundingMode.HALF_UP);List<SavingsDtos.ContributionView> items=contributions.findAllBySavingsGoalIdOrderByContributionDateDescIdDesc(goal.getId()).stream().map(SavingsService::response).toList();return new SavingsDtos.GoalView(goal.getId(),goal.getName(),goal.getTargetAmount(),contributed,remaining,progress,goal.getTargetDate(),goal.getDescription(),goal.getStatus(),items);}
    private SavingsGoal owned(String email,long id){return goals.findByIdAndUserId(id,account(email).getId()).orElseThrow(()->new IllegalArgumentException("Savings goal not found"));}
    private User account(String email){return users.findByEmail(email.toLowerCase()).orElseThrow(()->new IllegalStateException("Authenticated account was not found"));}
    private static String clean(String text){return text==null||text.isBlank()?null:text.trim();}
    private static SavingsDtos.ContributionView response(SavingsContribution contribution){return new SavingsDtos.ContributionView(contribution.getId(),contribution.getAmount(),contribution.getContributionDate(),contribution.getDescription());}
}
