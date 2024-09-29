package com.mbat.mbatapi.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime expiryDate;

    public VerificationToken(Long id, String token, User user, LocalDateTime expiryDate) {
        this.id = id;
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }

    public VerificationToken() {
    }

    public VerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusDays(1); // Jeton valide pendant 1 jour
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

}