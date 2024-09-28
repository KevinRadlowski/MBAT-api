package com.mbat.mbatapi.repository;
import com.mbat.mbatapi.entity.Month;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthRepository extends JpaRepository<Month, Long> {
}
