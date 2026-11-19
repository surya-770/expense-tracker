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
import com.expensetracker.dto.FinanceDtos;
import com.expensetracker.service.CategoryService;

@RestController @RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;
    public CategoryController(CategoryService service){this.service=service;}
    @GetMapping public List<FinanceDtos.CategoryResponse> list(Authentication auth){return service.list(auth.getName());}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public FinanceDtos.CategoryResponse create(@Valid @RequestBody FinanceDtos.CategoryRequest request,Authentication auth){return service.create(auth.getName(),request);}
    @PutMapping("/{id}") public FinanceDtos.CategoryResponse update(@PathVariable long id,@Valid @RequestBody FinanceDtos.CategoryRequest request,Authentication auth){return service.update(auth.getName(),id,request);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable long id,Authentication auth){service.delete(auth.getName(),id);}
}
