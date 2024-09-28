package com.mbat.mbatapi.controller;

import com.mbat.mbatapi.business.UserBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class AuthController {
    @GetMapping("/success")
    public ResponseEntity<String> loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        return ResponseEntity.ok("Connexion réussie !");
    }

    @GetMapping("/failure")
    public ResponseEntity<String> loginFailure() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Échec de la connexion");
    }

}