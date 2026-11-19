package com.expensetracker.entity;

import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity @Table(name="categories")
public class Category {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="user_id") private Long userId;
    @Column(nullable=false,length=80) private String name;
    @Column(nullable=false,length=8) private String type;
    @Column(nullable=false,length=40) private String icon="circle";
    @Column(nullable=false,length=7) private String color="#3478F6";
    @CreationTimestamp @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    protected Category() {}
    public Category(Long userId,String name,String type,String icon,String color){this.userId=userId;this.name=name;this.type=type;this.icon=icon;this.color=color;}
    public Long getId(){return id;} public Long getUserId(){return userId;} public String getName(){return name;} public String getType(){return type;} public String getIcon(){return icon;} public String getColor(){return color;}
    public void setName(String name){this.name=name;} public void setIcon(String icon){this.icon=icon;} public void setColor(String color){this.color=color;}
}
