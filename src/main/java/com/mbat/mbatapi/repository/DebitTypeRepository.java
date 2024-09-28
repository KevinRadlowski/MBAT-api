package com.mbat.mbatapi.repository;
import com.mbat.mbatapi.entity.DebitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebitTypeRepository extends JpaRepository<DebitType, Long> {
}
