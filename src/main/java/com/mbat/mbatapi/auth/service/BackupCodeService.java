package com.mbat.mbatapi.auth.service;

import com.mbat.mbatapi.auth.entity.BackupCode;
import com.mbat.mbatapi.auth.entity.User;
import com.mbat.mbatapi.repository.BackupCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service pour gérer les codes de secours des utilisateurs.
 */
@Service
public class BackupCodeService {
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private BackupCodeRepository backupCodeRepository;

    @Autowired EmailService emailService;

    /**
     * Génère une nouvelle liste de 10 codes de secours pour un utilisateur et remplace les anciens.
     *
     * @param user L'utilisateur pour lequel générer les codes.
     * @return La nouvelle liste de codes de secours.
     */
    public List<BackupCode> generateBackupCodesForUser(User user) {
        // Supprime les anciens codes
        backupCodeRepository.deleteByUser(user);

        // Génère 10 nouveaux codes
        List<BackupCode> backupCodes = IntStream.range(0, 10)
                .mapToObj(i -> {
                    BackupCode code = new BackupCode();
                    String rawCode = generateRandomCode();
                    code.setCode(passwordEncoder.encode(rawCode)); // Hachage du code                    code.setUser(user);
                    code.setCreatedAt(LocalDateTime.now());
                    code.setUsed(false);
                    return code;
                })
                .collect(Collectors.toList());

        backupCodeRepository.saveAll(backupCodes);

        // Envoie l'email de notification pour la régénération des codes de secours
        emailService.sendBackupCodesGeneratedEmail(user.getUsername());
        return backupCodes;

    }

    /**
     * Récupère tous les codes de secours d'un utilisateur.
     *
     * @param user L'utilisateur pour lequel récupérer les codes.
     * @return La liste des codes de secours.
     */
    public List<BackupCode> getBackupCodesForUser(User user) {
        return backupCodeRepository.findByUser(user);
    }

    /**
     * Utilise un code de secours s'il est valide et non utilisé.
     *
     * @param user L'utilisateur pour lequel utiliser le code.
     * @param rawCode Le code à utiliser.
     * @return True si le code a été utilisé avec succès, false sinon.
     */
    public boolean useBackupCode(User user, String rawCode) {
        Optional<BackupCode> backupCodeOpt = backupCodeRepository.findByUser(user)
                .stream()
                .filter(code -> passwordEncoder.matches(rawCode, code.getCode()) && !code.isUsed())
                .findFirst();

        if (backupCodeOpt.isPresent()) {
            BackupCode backupCode = backupCodeOpt.get();
            if (!backupCode.isUsed()) {
                backupCode.setUsed(true);
                backupCode.setUsedAt(LocalDateTime.now());
                backupCodeRepository.save(backupCode);

                // Envoie une notification à l'utilisateur avec une partie du code masqué
                emailService.sendBackupCodeUsedEmail(user.getUsername(), rawCode);

                return true;
            }
        }
        return false;
    }

    /**
     * Génère un code de secours aléatoire de 8 chiffres.
     *
     * @return Un code aléatoire.
     */
    private String generateRandomCode() {
        // Génère un code aléatoire à 8 chiffres
        return String.format("%08d", (int) (Math.random() * 100000000));
    }
}
