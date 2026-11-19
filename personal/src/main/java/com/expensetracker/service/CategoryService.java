package com.expensetracker.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.expensetracker.dto.FinanceDtos;
import com.expensetracker.entity.Category;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;

@Service
public class CategoryService {
    private final CategoryRepository categories; private final TransactionRepository transactions; private final UserRepository users;
    public CategoryService(CategoryRepository categories,TransactionRepository transactions,UserRepository users){this.categories=categories;this.transactions=transactions;this.users=users;}
    @Transactional(readOnly=true)
    public List<FinanceDtos.CategoryResponse> list(String email){long userId=userId(email);return categories.findAvailableForUser(userId).stream().map(CategoryService::response).toList();}
    @Transactional
    public FinanceDtos.CategoryResponse create(String email,FinanceDtos.CategoryRequest request){
        long userId=userId(email);String name=request.name().trim();String type=request.type().name();
        if(categories.findAvailableForUser(userId).stream().anyMatch(c->c.getType().equals(type)&&c.getName().equalsIgnoreCase(name)))throw new IllegalArgumentException("A category with this name already exists");
        Category saved=categories.save(new Category(userId,name,type,defaulted(request.icon(),"circle"),defaulted(request.color(),"#3478F6")));return response(saved);
    }
    @Transactional
    public FinanceDtos.CategoryResponse update(String email,long id,FinanceDtos.CategoryRequest request){
        Category category=owned(email,id);if(category.getUserId()==null)throw new IllegalArgumentException("Shared default categories cannot be edited");
        String name=request.name().trim();if(categories.findAvailableForUser(category.getUserId()).stream().anyMatch(c->!c.getId().equals(id)&&c.getType().equals(request.type().name())&&c.getName().equalsIgnoreCase(name)))throw new IllegalArgumentException("A category with this name already exists");
        if(!category.getType().equals(request.type().name()))throw new IllegalArgumentException("Category type cannot be changed after creation");
        category.setName(name);category.setIcon(defaulted(request.icon(),"circle"));category.setColor(defaulted(request.color(),"#3478F6"));return response(categories.save(category));
    }
    @Transactional
    public void delete(String email,long id){Category category=owned(email,id);if(category.getUserId()==null)throw new IllegalArgumentException("Shared default categories cannot be deleted");if(transactions.existsByCategory_Id(id))throw new IllegalArgumentException("This category is used by transactions. Reassign them before deleting it.");categories.delete(category);}
    private Category owned(String email,long id){return categories.findByIdAndUserId(id,userId(email)).orElseThrow(()->new IllegalArgumentException("Category not found"));}
    private long userId(String email){return users.findByEmail(email.toLowerCase()).orElseThrow(()->new IllegalStateException("Authenticated account was not found")).getId();}
    private static String defaulted(String value,String fallback){return value==null||value.isBlank()?fallback:value;}
    private static FinanceDtos.CategoryResponse response(Category c){return new FinanceDtos.CategoryResponse(c.getId(),c.getName(),com.expensetracker.entity.TransactionType.valueOf(c.getType()),c.getIcon(),c.getColor(),c.getUserId()==null);}
}
