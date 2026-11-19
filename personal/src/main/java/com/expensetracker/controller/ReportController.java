package com.expensetracker.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.expensetracker.dto.BudgetDtos;
import com.expensetracker.dto.DashboardDtos;
import com.expensetracker.dto.ReportDtos;
import com.expensetracker.dto.SavingsDtos;
import com.expensetracker.entity.PaymentMethod;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.service.ReportService;

@RestController @RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reports;
    public ReportController(ReportService reports){this.reports=reports;}
    @GetMapping("/monthly") public DashboardDtos.Summary monthly(Authentication auth,@RequestParam(required=false) Integer year,@RequestParam(required=false) Integer month){YearMonth now=YearMonth.now();return reports.monthly(auth.getName(),year==null?now.getYear():year,month==null?now.getMonthValue():month);}
    @GetMapping("/yearly") public ReportDtos.YearlyReport yearly(Authentication auth,@RequestParam(required=false) Integer year){return reports.yearly(auth.getName(),year==null?YearMonth.now().getYear():year);}
    @GetMapping("/categories") public List<DashboardDtos.CategoryPoint> categories(Authentication auth,@RequestParam LocalDate startDate,@RequestParam LocalDate endDate){return reports.categories(auth.getName(),startDate,endDate);}
    @GetMapping("/budget") public BudgetDtos.MonitoringView budget(Authentication auth,@RequestParam(required=false) Integer year,@RequestParam(required=false) Integer month){YearMonth now=YearMonth.now();return reports.budget(auth.getName(),year==null?now.getYear():year,month==null?now.getMonthValue():month);}
    @GetMapping("/savings") public List<SavingsDtos.GoalView> savings(Authentication auth){return reports.savings(auth.getName());}
    @GetMapping("/custom") public ReportDtos.CustomReport custom(Authentication auth,@RequestParam LocalDate startDate,@RequestParam LocalDate endDate,
            @RequestParam(required=false) TransactionType type,@RequestParam(required=false) Long categoryId,@RequestParam(required=false) PaymentMethod paymentMethod){return reports.custom(auth.getName(),startDate,endDate,type,categoryId,paymentMethod);}
    @GetMapping(value="/export",produces="text/csv") public ResponseEntity<String> export(Authentication auth,@RequestParam String type,
            @RequestParam(required=false) Integer year,@RequestParam(required=false) Integer month,
            @RequestParam(required=false) LocalDate startDate,@RequestParam(required=false) LocalDate endDate,
            @RequestParam(required=false) TransactionType transactionType,@RequestParam(required=false) Long categoryId,
            @RequestParam(required=false) PaymentMethod paymentMethod){YearMonth now=YearMonth.now();int selectedYear=year==null?now.getYear():year,selectedMonth=month==null?now.getMonthValue():month;String csv=reports.export(auth.getName(),type,selectedYear,selectedMonth,startDate,endDate,transactionType,categoryId,paymentMethod);return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=financial-report-"+type.toLowerCase()+".csv").contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(csv);}
}
