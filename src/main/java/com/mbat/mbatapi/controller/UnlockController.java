package com.mbat.mbatapi.controller;

import com.mbat.mbatapi.entity.User;
import com.mbat.mbatapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UnlockController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/unlock")
    public ResponseEntity<String> unlockAccount(@RequestParam String token) {
        Optional<User> userOpt = userRepository.findByUnlockToken(token);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setAccountLocked(false);
            user.setFailedAttempts(0);
            user.setLockTime(null);
            user.setUnlockToken(null);
            userRepository.save(user);
            return ResponseEntity.ok("Compte déverrouillé avec succès.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token invalide.");
        }
    }
}

