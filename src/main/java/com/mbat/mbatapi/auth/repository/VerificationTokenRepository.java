package com.mbat.mbatapi.auth.repository;

import com.mbat.mbatapi.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mbat.mbatapi.auth.entity.VerificationToken;

import java.util.List;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);


    List<VerificationToken> findAllByUser(User user);
}