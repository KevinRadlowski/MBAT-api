package com.mbat.mbatapi.entity;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mbat.mbatapi.exception.InvalidEmailException;
import com.mbat.mbatapi.exception.InvalidPasswordException;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Email
    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 4, max = 64)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(String username, String password)
            throws InvalidEmailException, InvalidPasswordException {
        this.setUsername(username);
        this.setPassword(password);
    }

    public User() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) throws InvalidParameterException, InvalidEmailException {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(username);
        if (username == null) {
            throw new InvalidParameterException("Le champ ne peut être vide.");
        }

        if (matcher.matches()) {
            this.username = username;
        } else {
            throw new InvalidEmailException();
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) throws InvalidPasswordException {
        // if
        // (password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()\\[\\]{}:;',?/*~$^+=<>]).{8,20}$"))
        // {
        // System.out.println("Le mot de passe est valide !");
        this.password = password;
        // } else {
        // throw new InvalidPasswordException();
        // }
    }

    public Set<Role> getRoles() {
        return roles;
      }
    
      public void setRoles(Set<Role> roles) {
        this.roles = roles;
      }
}