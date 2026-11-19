package com.expensetracker.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.expensetracker.dto.InterestDtos;
import com.expensetracker.entity.InterestType;
import com.expensetracker.service.InterestService;

@RestController @RequestMapping("/api/interest")
public class InterestController {
    private final InterestService service;
    public InterestController(InterestService service){this.service=service;}
    @PostMapping("/simple") public InterestDtos.Result simple(@Valid @RequestBody InterestDtos.Request request){return service.calculate(InterestType.SIMPLE,request);}
    @PostMapping("/compound") public InterestDtos.Result compound(@Valid @RequestBody InterestDtos.Request request){return service.calculate(InterestType.COMPOUND,request);}
    @PostMapping("/history") @ResponseStatus(HttpStatus.CREATED) public InterestDtos.Result save(Authentication auth,@Valid @RequestBody InterestDtos.SaveRequest request){return service.save(auth.getName(),request.type(),request.calculation());}
    @GetMapping("/history") public List<InterestDtos.Result> history(Authentication auth,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.history(auth.getName(),page,size);}
    @DeleteMapping("/history/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(Authentication auth,@PathVariable long id){service.delete(auth.getName(),id);}
    @GetMapping(value="/history/export",produces="text/csv") public ResponseEntity<byte[]> export(Authentication auth){byte[] csv=service.exportCsv(auth.getName()).getBytes(StandardCharsets.UTF_8);return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=interest-calculations.csv").contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(csv);}
}
