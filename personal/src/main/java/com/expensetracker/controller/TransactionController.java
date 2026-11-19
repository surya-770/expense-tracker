package com.expensetracker.controller;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.expensetracker.dto.FinanceDtos;
import com.expensetracker.entity.PaymentMethod;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.service.TransactionService;

@RestController @RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService service;
    public TransactionController(TransactionService service){this.service=service;}
    @GetMapping public Page<FinanceDtos.TransactionResponse> list(Authentication auth,
            @RequestParam(required=false) TransactionType type,@RequestParam(required=false) Long categoryId,
            @RequestParam(required=false) PaymentMethod paymentMethod,@RequestParam(required=false) LocalDate startDate,
            @RequestParam(required=false) LocalDate endDate,@RequestParam(defaultValue="") String search,
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="date") String sortBy,@RequestParam(defaultValue="desc") String direction){
        return service.search(auth.getName(),type,categoryId,paymentMethod,startDate,endDate,search,page,size,sortBy,direction);
    }
    @GetMapping("/{id}") public FinanceDtos.TransactionResponse get(@PathVariable long id,Authentication auth){return service.get(auth.getName(),id);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public FinanceDtos.TransactionResponse create(@Valid @RequestBody FinanceDtos.TransactionRequest request,Authentication auth){return service.create(auth.getName(),request);}
    @PutMapping("/{id}") public FinanceDtos.TransactionResponse update(@PathVariable long id,@Valid @RequestBody FinanceDtos.TransactionRequest request,Authentication auth){return service.update(auth.getName(),id,request);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable long id,Authentication auth){service.delete(auth.getName(),id);}
    @GetMapping(value="/export",produces="text/csv") public ResponseEntity<String> export(Authentication auth,
            @RequestParam(required=false) TransactionType type,@RequestParam(required=false) Long categoryId,
            @RequestParam(required=false) PaymentMethod paymentMethod,@RequestParam(required=false) LocalDate startDate,
            @RequestParam(required=false) LocalDate endDate,@RequestParam(defaultValue="") String search){
        String csv=service.exportCsv(auth.getName(),type,categoryId,paymentMethod,startDate,endDate,search);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=transactions.csv").contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(csv);
    }
}
