package com.expensetracker.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.expensetracker.dto.SavingsDtos;
import com.expensetracker.entity.GoalStatus;
import com.expensetracker.service.SavingsService;

@RestController @RequestMapping("/api/savings-goals")
public class SavingsController {
    private final SavingsService savings;
    public SavingsController(SavingsService savings){this.savings=savings;}
    @GetMapping public List<SavingsDtos.GoalView> list(Authentication auth){return savings.list(auth.getName());}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public SavingsDtos.GoalView create(Authentication auth,@Valid @RequestBody SavingsDtos.GoalRequest request){return savings.create(auth.getName(),request);}
    @GetMapping("/{id}") public SavingsDtos.GoalView get(Authentication auth,@PathVariable long id){return savings.get(auth.getName(),id);}
    @PutMapping("/{id}") public SavingsDtos.GoalView update(Authentication auth,@PathVariable long id,@Valid @RequestBody SavingsDtos.GoalRequest request){return savings.update(auth.getName(),id,request);}
    @PutMapping("/{id}/status") public SavingsDtos.GoalView status(Authentication auth,@PathVariable long id,@Valid @RequestBody SavingsDtos.StatusRequest request){return savings.setStatus(auth.getName(),id,request.status());}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(Authentication auth,@PathVariable long id){savings.delete(auth.getName(),id);}
    @GetMapping("/{id}/contributions") public List<SavingsDtos.ContributionView> history(Authentication auth,@PathVariable long id){return savings.history(auth.getName(),id);}
    @PostMapping("/{id}/contributions") @ResponseStatus(HttpStatus.CREATED) public SavingsDtos.ContributionView add(Authentication auth,@PathVariable long id,@Valid @RequestBody SavingsDtos.ContributionRequest request){return savings.add(auth.getName(),id,request);}
    @PutMapping("/{id}/contributions/{contributionId}") public SavingsDtos.ContributionView updateContribution(Authentication auth,@PathVariable long id,@PathVariable long contributionId,@Valid @RequestBody SavingsDtos.ContributionRequest request){return savings.updateContribution(auth.getName(),id,contributionId,request);}
    @DeleteMapping("/{id}/contributions/{contributionId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteContribution(Authentication auth,@PathVariable long id,@PathVariable long contributionId){savings.deleteContribution(auth.getName(),id,contributionId);}
}
