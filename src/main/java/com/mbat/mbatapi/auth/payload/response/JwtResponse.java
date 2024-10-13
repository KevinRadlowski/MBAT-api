package com.mbat.mbatapi.auth.payload.response;

import java.util.List;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Integer id;
    private String username;
    private List<String> roles;

    private String refreshToken; // Ajoute le refresh token ici

    public JwtResponse(String accessToken, String refreshToken, Integer id, String username, List<String> roles) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }
//    public JwtResponse(String accessToken, Integer id, String username, List<String> roles, String refreshToken) {
//        this.token = accessToken;
//        this.id = id;
//        this.username = username;
//        this.roles = roles;
//        this.refreshToken = refreshToken; // Assigner le refresh token ici
//    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}