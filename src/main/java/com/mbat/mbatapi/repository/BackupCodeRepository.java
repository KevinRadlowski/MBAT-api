package com.mbat.mbatapi.repository;

import com.mbat.mbatapi.auth.entity.BackupCode;
import com.mbat.mbatapi.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BackupCodeRepository extends JpaRepository<BackupCode, Long> {
    List<BackupCode> findByUserAndUsedAtIsNull(User user);
    Optional<BackupCode> findByUserAndCodeAndUsedAtIsNull(User user, String code);
    void deleteByUser(User user);

    List<BackupCode> findByUser(User user);

    Optional<BackupCode> findByUserAndCode(User user, String code);
}