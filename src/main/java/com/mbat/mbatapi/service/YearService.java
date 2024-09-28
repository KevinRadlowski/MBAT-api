package com.mbat.mbatapi.service;
import com.mbat.mbatapi.entity.Year;
import com.mbat.mbatapi.repository.YearRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class YearService {

    @Autowired
    private YearRepository yearRepository;

    public List<Year> getAllYears() {
        return yearRepository.findAll();
    }

    public Optional<Year> getYearById(Long id) {
        return yearRepository.findById(id);
    }

    public Year createOrUpdateYear(Year year) {
        return yearRepository.save(year);
    }

    public void deleteYear(Long id) {
        yearRepository.deleteById(id);
    }
}
