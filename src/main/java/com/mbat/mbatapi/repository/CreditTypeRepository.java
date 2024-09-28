package com.mbat.mbatapi.repository;
import com.mbat.mbatapi.entity.CreditType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditTypeRepository extends JpaRepository<CreditType, Long> {
}
