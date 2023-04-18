package com.mbat.mbatapi.payload.request;

import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignupRequest {
    @NotBlank
    @Email
    private String username;

    private Set<String> role;

    @NotBlank
    @Size(min = 4, max = 64)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRole() {
        return this.role;
    }

    public void setRole(Set<String> role) {
        this.role = role;
    }
}