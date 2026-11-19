package com.expensetracker.controller;

import java.time.YearMonth;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.expensetracker.dto.BudgetDtos;
import com.expensetracker.service.BudgetService;

@RestController @RequestMapping("/api/budgets")
public class BudgetController {
    private final BudgetService service;
    public BudgetController(BudgetService service){this.service=service;}
    @GetMapping public List<BudgetDtos.PlanView> list(Authentication auth,@RequestParam(required=false) Integer year){return service.list(auth.getName(),year);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public BudgetDtos.PlanView create(Authentication auth,@Valid @RequestBody BudgetDtos.PlanRequest request){return service.create(auth.getName(),request);}
    @GetMapping("/{id}") public BudgetDtos.PlanView get(Authentication auth,@PathVariable long id){return service.get(auth.getName(),id);}
    @PutMapping("/{id}") public BudgetDtos.PlanView update(Authentication auth,@PathVariable long id,@Valid @RequestBody BudgetDtos.PlanRequest request){return service.update(auth.getName(),id,request);}
    @GetMapping("/monitoring") public BudgetDtos.MonitoringView monitoring(Authentication auth,@RequestParam(required=false) Integer month,@RequestParam(required=false) Integer year){YearMonth now=YearMonth.now();return service.monitoring(auth.getName(),year==null?now.getYear():year,month==null?now.getMonthValue():month);}
}
