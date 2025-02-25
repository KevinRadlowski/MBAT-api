package com.mbat.mbatapi.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EncryptionService {
    private final SecretKey secretKey;

    public EncryptionService(@Value("${encryption.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(), "AES");
    }

    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chiffrement des données", e);
        }
    }

//    public String decrypt(String encryptedData) {
//        if (encryptedData == null) {
//            return null;
//        }
//        try {
//            Cipher cipher = Cipher.getInstance("AES");
//            cipher.init(Cipher.DECRYPT_MODE, secretKey);
//            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
//        } catch (Exception e) {
//            throw new RuntimeException("Erreur lors du déchiffrement des données", e);
//        }
//    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
        } catch (IllegalArgumentException e) {
            // Si la chaîne n'est pas valide pour Base64, renvoie null ou une valeur par défaut
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du déchiffrement des données", e);
        }
    }

}
