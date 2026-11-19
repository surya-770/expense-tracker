package com.expensetracker.controller;

import java.time.YearMonth;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.expensetracker.dto.DashboardDtos;
import com.expensetracker.dto.FinanceDtos;
import com.expensetracker.service.DashboardService;

@RestController @RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboard;
    public DashboardController(DashboardService dashboard){this.dashboard=dashboard;}
    @GetMapping("/summary") public DashboardDtos.Summary summary(Authentication auth,@RequestParam(required=false) Integer month,@RequestParam(required=false) Integer year){
        YearMonth period=month==null&&year==null?YearMonth.now():YearMonth.of(year==null?YearMonth.now().getYear():year,month==null?YearMonth.now().getMonthValue():month);return dashboard.summary(auth.getName(),period);
    }
    @GetMapping("/charts") public DashboardDtos.Charts charts(Authentication auth,@RequestParam(required=false) Integer month,@RequestParam(required=false) Integer year){
        YearMonth now=YearMonth.now();YearMonth period=YearMonth.of(year==null?now.getYear():year,month==null?now.getMonthValue():month);return dashboard.charts(auth.getName(),period);
    }
    @GetMapping("/recent-transactions") public List<FinanceDtos.TransactionResponse> recent(Authentication auth,@RequestParam(defaultValue="5") int limit){return dashboard.recent(auth.getName(),limit);}
}
