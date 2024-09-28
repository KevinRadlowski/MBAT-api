package com.mbat.mbatapi.repository;
import com.mbat.mbatapi.entity.CashFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashFlowRepository extends JpaRepository<CashFlow, Long> {
}
