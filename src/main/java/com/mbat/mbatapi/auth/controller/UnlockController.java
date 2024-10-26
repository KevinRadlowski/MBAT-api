package com.mbat.mbatapi.auth.controller;

import com.mbat.mbatapi.auth.entity.User;
import com.mbat.mbatapi.auth.payload.response.MessageResponse;
import com.mbat.mbatapi.auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

//    @GetMapping("/unlock")
//    public ResponseEntity<String> unlockAccount(@RequestParam String token) {
//        Optional<User> userOpt = userRepository.findByUnlockToken(token);
//
//        if (userOpt.isPresent()) {
//            User user = userOpt.get();
//            user.setAccountLocked(false);
//            user.setFailedAttempts(0);
//            user.setLockTime(null);
//            user.setUnlockToken(null);
//            userRepository.save(user);
//            return ResponseEntity.ok("Compte déverrouillé avec succès.");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token invalide.");
//        }
//    }

    @Operation(summary = "Déverrouillage du compte", description = "Déverrouille un compte utilisateur à l'aide d'un jeton de déverrouillage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compte déverrouillé avec succès."),
            @ApiResponse(responseCode = "400", description = "Jeton de déverrouillage invalide ou expiré.")
    })
    @GetMapping("/unlock")
    public ResponseEntity<?> unlockAccount(@RequestParam String token) {
        try {
            // Recherche de l'utilisateur avec le token de déverrouillage
            Optional<User> userOpt = userRepository.findByUnlockToken(token);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Jeton de déverrouillage invalide ou expiré."));
            }

            User user = userOpt.get();

            // Déverrouille le compte si le jeton est valide
            user.setAccountLocked(false);
            user.setFailedAttempts(0);
            user.setLockTime(null);
            user.setUnlockToken(null);
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("Compte déverrouillé avec succès."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erreur lors du déverrouillage du compte."));
        }
    }
}

