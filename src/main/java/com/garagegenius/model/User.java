package com.garagegenius.model;

import java.time.LocalDateTime;
/**
 * Application user model.
 *
 * <p>Represents an authenticated identity in the system with a role ({@code admin}/{@code staff}/{@code customer})
 * and a status ({@code active}/{@code inactive}/{@code pending}). The {@code password} field holds the stored
 * password hash (BCrypt), not plaintext.</p>
 */
public class User {

    private int userId;
    private String fullName;
    private String email;
    private String password;
    private String role;
    private String phone;
    private LocalDateTime createdAt;
    private String status;

    public User() {}

    public User(int userId, String fullName, String email,
                String password, String role, String phone,
                LocalDateTime createdAt, String status) {
        this.userId    = userId;
        this.fullName  = fullName;
        this.email     = email;
        this.password  = password;
        this.role      = role;
        this.phone     = phone;
        this.createdAt = createdAt;
        this.status    = status;
    }

    public int getUserId(){
        return userId;
    }
    public void setUserId(int userId){
        this.userId = userId;
    }

    public String getFullName(){
        return fullName;
    }
    public void setFullName(String fullName){
        this.fullName = fullName;
    }

    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }

    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role){
        this.role = role;
    }

    public String getPhone(){
        return phone;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status = status;
    }
}