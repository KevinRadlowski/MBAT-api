package com.mbat.mbatapi.repository;

import com.mbat.mbatapi.entity.RecurringOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface RecurringOperationRepository extends JpaRepository<RecurringOperation, Long> {
}
