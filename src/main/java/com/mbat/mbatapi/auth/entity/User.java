package com.mbat.mbatapi.auth.entity;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mbat.mbatapi.auth.exception.InvalidEmailException;
import com.mbat.mbatapi.auth.exception.InvalidPasswordException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "phone")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 4, max = 64)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private boolean isVerified = false;
    private String theme = "theme-default";

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    private int failedAttempts;
    private boolean accountLocked;
    private Date lockTime;
    private String unlockToken;

    // Ajoute la relation pour `PasswordResetToken` avec suppression en cascade
    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private PasswordResetToken passwordResetToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<RefreshToken> refreshTokens;

    // Suppression en cascade pour les tokens de vérification
    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private VerificationToken verificationToken;

    @Column(name = "is_two_factor_enabled")
    private Boolean isTwoFactorEnabled = false;

    @Column(name = "first_two_factor_method")
    private String firstTwoFactorMethod; // "google_authenticator" ou "sms"

    @Column(name = "second_two_factor_method")
    private String secondTwoFactorMethod; // "google_authenticator" ou "sms"

    @Column(name = "first_two_factor_secret")
    private String firstTwoFactorSecret;  // Stocke le secret TOTP pour l'authentification Google Authenticator

    @Column(name = "second_two_factor_secret")
    private String secondTwoFactorSecret;  // Stocke le secret TOTP pour l'authentification Google Authenticator

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<BackupCode> backupCodes;

    @Column(name = "password_last_updated")
    private Date passwordLastUpdated;

    private String phone;

    private String firstName;
    private String lastName;
    private String securityQuestion;
    private String securityAnswer;


    // Constructeur principal
    public User(String identifier, String password) throws InvalidEmailException, InvalidPasswordException {
        this.setIdentifier(identifier);
        this.setPassword(password);
    }

    public User() {
    }

    public void setIdentifier(String identifier) throws InvalidParameterException, InvalidEmailException {
        // Regex pour l'email
        String emailRegex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}";
        Pattern emailPattern = Pattern.compile(emailRegex);

        if (emailPattern.matcher(identifier).matches()) {
            this.username = identifier;
        } else if (identifier.matches("^[0-9]{10}$")) {
            this.phone = identifier;
        } else {
            throw new InvalidParameterException("Identifiant invalide : doit être un email ou un numéro de téléphone.");
        }
    }


    public void setUsername(String username) throws InvalidParameterException, InvalidEmailException {
        if (username == null || username.isEmpty()) {
            throw new InvalidParameterException("Le champ 'username' ne peut être vide.");
        }

        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(username);

        if (matcher.matches()) {
            this.username = username;
        } else {
            throw new InvalidEmailException();
        }
    }



    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isTwoFactorEnabled() {
        return isTwoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        isTwoFactorEnabled = twoFactorEnabled;
    }
}