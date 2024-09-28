package com.mbat.mbatapi.business;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

import com.mbat.mbatapi.entity.*;
import com.mbat.mbatapi.repository.PasswordResetTokenRepository;
import com.mbat.mbatapi.repository.VerificationTokenRepository;
import com.mbat.mbatapi.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mbat.mbatapi.auth.ChangePasswordDto;
import com.mbat.mbatapi.exception.InvalidEmailException;
import com.mbat.mbatapi.exception.InvalidPasswordException;
import com.mbat.mbatapi.payload.request.LoginRequest;
import com.mbat.mbatapi.payload.request.SignupRequest;
import com.mbat.mbatapi.payload.response.JwtResponse;
import com.mbat.mbatapi.payload.response.MessageResponse;
import com.mbat.mbatapi.repository.RoleRepository;
import com.mbat.mbatapi.repository.UserRepository;
import com.mbat.mbatapi.security.jwt.JwtUtils;
import com.mbat.mbatapi.security.services.UserDetailsImpl;

@Service
public class UserBusiness {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService; // Service d'envoi d'email

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;


    @Autowired
    JwtUtils jwtUtils;

    public void changePassword(ChangePasswordDto passwordDto, User user) throws InvalidPasswordException {
        if (!encoder.matches(passwordDto.getOldPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        user.setPassword(passwordDto.getNewPassword());
        hashPassword(user);

        userRepository.save(user);
    }

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        // Cherchez l'utilisateur par nom d'utilisateur
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Utilisateur non trouvé."));
        }

        User user = userOpt.get();

        // Vérifiez si le compte est vérifié
        if (!user.isVerified()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Le compte n'est pas encore vérifié."));
        }

        // Authentification
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                roles));
    }

    public ResponseEntity<?> registerUser(SignupRequest signUpRequest)
            throws InvalidPasswordException, InvalidEmailException {

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Cet utilisateur existe déjà."));
        }

        // Crée un nouveau compte utilisateur
        User user = new User(signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()));
        user.setVerified(false); // Par défaut, non vérifié

        // Définir le rôle de l'utilisateur sur ROLE_USER par défaut
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Le rôle USER n'est pas défini."));
        roles.add(userRole);
        user.setRoles(roles);

        // Sauvegarde l'utilisateur dans la base de données
        userRepository.save(user);


        // Créer un jeton de vérification
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);

        // Envoyer un email de vérification
        String verificationLink = "http://192.168.56.101:4200/verify-email?token=" + token;
        emailService.sendVerificationEmail(user.getUsername(), verificationLink);

        return ResponseEntity.ok(new MessageResponse("Utilisateur enregistré avec succès! Veuillez vérifier votre email pour activer votre compte."));
    }

    private void hashPassword(User user) throws InvalidPasswordException {
        String hashed = encoder.encode(user.getPassword());
        user.setPassword(hashed);
    }

    public ResponseEntity<HttpStatus> deleteUser(Integer id) {
        try {
            userRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<User> updateInformationUser(Integer id, User user) throws InvalidParameterException, InvalidEmailException {
        Optional<User> user_update = userRepository.findById(id);
        User __user = user_update.get();
        if (user_update.isPresent()) {
            __user.setUsername(user.getUsername());
            return new ResponseEntity<>(userRepository.save(__user), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Ajoutez la méthode processForgotPassword ici
    public void processForgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByUsername(email);
        if (userOptional.isEmpty()) { // Utilisez isEmpty() pour vérifier si l'Optional est vide
            throw new UsernameNotFoundException("Utilisateur non trouvé");
        }

        User user = userOptional.get(); // Extraire l'utilisateur de l'Optional
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://192.168.56.101:4200/reset-password?token=" + token; // Votre URL Angular
        emailService.sendResetPasswordEmail(user.getUsername(), resetLink);
    }
    // Ajoutez la méthode updatePassword ici
    public void updatePassword(String token, String newPassword) throws InvalidPasswordException {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
        if (resetToken == null || resetToken.isExpired()) {
            throw new IllegalArgumentException("Jeton invalide ou expiré");
        }

        User user = resetToken.getUser();
        if (user == null) {
            throw new IllegalArgumentException("Utilisateur non trouvé pour ce jeton");
        }

        // Validez le nouveau mot de passe s'il y a des règles spécifiques (par exemple, longueur, caractères spéciaux, etc.)
        if (newPassword == null || newPassword.isEmpty()) {
            throw new InvalidPasswordException("Le mot de passe ne peut pas être vide");
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken); // Supprimez le jeton après utilisation
    }


}
