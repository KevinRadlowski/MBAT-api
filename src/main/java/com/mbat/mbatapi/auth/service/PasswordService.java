package com.mbat.mbatapi.auth.service;

import com.mbat.mbatapi.auth.ChangePasswordDto;
import com.mbat.mbatapi.auth.entity.PasswordResetToken;
import com.mbat.mbatapi.auth.entity.User;
import com.mbat.mbatapi.auth.exception.InvalidPasswordException;
import com.mbat.mbatapi.auth.repository.PasswordResetTokenRepository;
import com.mbat.mbatapi.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordService {

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Change le mot de passe de l'utilisateur.
     *
     * @param passwordDto Les informations de changement de mot de passe.
     * @param user        L'utilisateur pour lequel changer le mot de passe.
     * @throws InvalidPasswordException Si l'ancien mot de passe est incorrect.
     */
    public void changePassword(ChangePasswordDto passwordDto, User user) throws InvalidPasswordException {
        if (!encoder.matches(passwordDto.getOldPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        user.setPassword(encoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Met à jour le mot de passe d'un utilisateur.
     *
     * @param token       Le jeton de réinitialisation de mot de passe.
     * @param newPassword Le nouveau mot de passe.
     * @throws InvalidPasswordException Si le mot de passe est invalide.
     */
    public void updatePassword(String token, String newPassword) throws InvalidPasswordException {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
        if (resetToken == null || resetToken.isExpired()) {
            throw new IllegalArgumentException("Jeton invalide ou expiré");
        }

        User user = resetToken.getUser();

        // Validez le nouveau mot de passe s'il y a des règles spécifiques (par exemple, longueur, caractères spéciaux, etc.)
        if (newPassword == null || newPassword.isEmpty()) {
            throw new InvalidPasswordException("Le mot de passe ne peut pas être vide");
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken); // Supprimez le jeton après utilisation
    }

    public boolean checkOldPassword(User user, String oldPassword) {
        // Utilise le PasswordEncoder pour comparer le mot de passe fourni et celui dans la base de données
        return encoder.matches(oldPassword, user.getPassword());
    }

    public void updateUserPassword(Long id, String newPassword) throws InvalidPasswordException {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Validez le nouveau mot de passe ici (par exemple, vérifiez sa complexité)
            if (newPassword == null || newPassword.isEmpty()) {
                throw new InvalidPasswordException("Le mot de passe ne peut pas être vide.");
            }

            // Encodage et mise à jour du mot de passe
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
        } else {
            throw new InvalidPasswordException("Utilisateur non trouvé.");
        }
    }

    /**
     * Gère le processus de réinitialisation du mot de passe.
     *
     * @param email L'email de l'utilisateur pour lequel réinitialiser le mot de passe.
     */
    public void processForgotPassword(String email) {
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://192.168.56.101:4200/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(user.getUsername(), resetLink);
    }
}
