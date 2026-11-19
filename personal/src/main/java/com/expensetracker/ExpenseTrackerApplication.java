package com.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.expensetracker.desktop.ExpenseTrackerDesktop;

@SpringBootApplication
public class ExpenseTrackerApplication {
    public static void main(String[] args) {
        if (java.util.Arrays.asList(args).contains("--desktop")) {
            ExpenseTrackerDesktop.launch();
            return;
        }
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }
}
