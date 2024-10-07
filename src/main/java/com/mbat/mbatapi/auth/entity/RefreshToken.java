package com.mbat.mbatapi.auth.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;

@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean isExpired;

    @Column(nullable = false)
    private boolean isRevoked;  // Si tu souhaites gérer explicitement la révocation

    // Expiration Date and other fields
    private Date expiryDate;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isExpired() {
        return isExpired || new Date().after(this.expiryDate);
    }

    public void setExpired(boolean expired) {
        this.isExpired = expired;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        this.isRevoked = revoked;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}