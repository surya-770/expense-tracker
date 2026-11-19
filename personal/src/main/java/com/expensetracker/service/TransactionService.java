package com.expensetracker.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.expensetracker.dto.FinanceDtos;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.util.CsvUtil;

@Service
public class TransactionService {
    private final TransactionRepository transactions;private final CategoryRepository categories;private final UserRepository users;
    public TransactionService(TransactionRepository transactions,CategoryRepository categories,UserRepository users){this.transactions=transactions;this.categories=categories;this.users=users;}

    @Transactional(readOnly=true)
    public Page<FinanceDtos.TransactionResponse> search(String email,TransactionType type,Long categoryId,com.expensetracker.entity.PaymentMethod method,
            LocalDate start,LocalDate end,String query,int page,int size,String sortBy,String direction){
        if(page<0||size<1||size>100)throw new IllegalArgumentException("Page must be zero or greater and page size must be between 1 and 100");
        if(start!=null&&end!=null&&start.isAfter(end))throw new IllegalArgumentException("Start date must be on or before end date");
        String property=switch(sortBy==null?"date":sortBy){case "amount"->"amount";case "category"->"category.name";case "date"->"transactionDate";default->throw new IllegalArgumentException("Unsupported sort field");};
        Sort sort=Sort.by("desc".equalsIgnoreCase(direction)?Sort.Direction.DESC:Sort.Direction.ASC,property);
        Pageable pageable=PageRequest.of(page,size,sort);
        return transactions.search(userId(email),type,categoryId,method,start,end,query==null?"":query.trim(),pageable).map(TransactionService::response);
    }
    @Transactional(readOnly=true)
    public FinanceDtos.TransactionResponse get(String email,long id){return response(owned(email,id));}
    @Transactional
    public FinanceDtos.TransactionResponse create(String email,FinanceDtos.TransactionRequest request){
        var user=users.findByEmail(email.toLowerCase()).orElseThrow(()->new IllegalStateException("Authenticated account was not found"));
        Category category=availableCategory(user.getId(),request.categoryId(),request.type());validateMethod(request.type(),request.paymentMethod(),request.source());
        Transaction saved=transactions.save(new Transaction(user,category,request.type(),request.amount(),request.date(),clean(request.source()),request.type()==TransactionType.EXPENSE?request.paymentMethod():null,clean(request.description())));
        return response(saved);
    }
    @Transactional
    public FinanceDtos.TransactionResponse update(String email,long id,FinanceDtos.TransactionRequest request){
        Transaction transaction=owned(email,id);Category category=availableCategory(transaction.getUser().getId(),request.categoryId(),request.type());validateMethod(request.type(),request.paymentMethod(),request.source());
        transaction.update(category,request.type(),request.amount(),request.date(),clean(request.source()),request.type()==TransactionType.EXPENSE?request.paymentMethod():null,clean(request.description()));
        return response(transactions.save(transaction));
    }
    @Transactional
    public void delete(String email,long id){transactions.delete(owned(email,id));}
    @Transactional(readOnly=true)
    public String exportCsv(String email,TransactionType type,Long categoryId,com.expensetracker.entity.PaymentMethod method,LocalDate start,LocalDate end,String query){
        long uid=userId(email);if(start!=null&&end!=null&&start.isAfter(end))throw new IllegalArgumentException("Start date must be on or before end date");StringBuilder csv=new StringBuilder("\uFEFF").append(CsvUtil.row("transaction_id","date","description","category","type","payment_method_or_source","amount"));int page=0;org.springframework.data.domain.Page<Transaction> batch;
        do{batch=transactions.search(uid,type,categoryId,method,start,end,query==null?"":query.trim(),PageRequest.of(page++,500,Sort.by(Sort.Direction.DESC,"transactionDate")));for(Transaction t:batch.getContent())csv.append(CsvUtil.row(t.getId(),t.getTransactionDate(),t.getDescription(),t.getCategory().getName(),t.getTransactionType(),t.getTransactionType()==TransactionType.INCOME?t.getSource():t.getPaymentMethod(),t.getAmount().toPlainString()));}while(batch.hasNext());return csv.toString();
    }
    private Transaction owned(String email,long id){return transactions.findByIdAndUserId(id,userId(email)).orElseThrow(()->new IllegalArgumentException("Transaction not found"));}
    private Category availableCategory(long userId,long categoryId,TransactionType type){
        Category category=categories.findAvailableForUser(userId).stream().filter(c->c.getId().equals(categoryId)).findFirst().orElseThrow(()->new IllegalArgumentException("Category not found"));
        if(!category.getType().equals(type.name()))throw new IllegalArgumentException("Category type must match transaction type");return category;
    }
    private long userId(String email){return users.findByEmail(email.toLowerCase()).orElseThrow(()->new IllegalStateException("Authenticated account was not found")).getId();}
    private static void validateMethod(TransactionType type,com.expensetracker.entity.PaymentMethod method,String source){if(type==TransactionType.EXPENSE&&method==null)throw new IllegalArgumentException("Payment method is required for expenses");if(type==TransactionType.INCOME&&(source==null||source.isBlank()))throw new IllegalArgumentException("Income source is required");}
    private static String clean(String value){return value==null||value.isBlank()?null:value.trim();}
    private static FinanceDtos.TransactionResponse response(Transaction t){return new FinanceDtos.TransactionResponse(t.getId(),t.getTransactionDate(),t.getDescription(),t.getTransactionType(),t.getCategory().getId(),t.getCategory().getName(),t.getSource(),t.getPaymentMethod(),t.getAmount());}
}
