package com.mbat.mbatapi.service;
import com.mbat.mbatapi.entity.Month;
import com.mbat.mbatapi.repository.MonthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MonthService {

    @Autowired
    private MonthRepository monthRepository;

    public List<Month> getAllMonth() {
        return monthRepository.findAll();
    }

    public Optional<Month> getMonthById(Long id) {
        return monthRepository.findById(id);
    }

    public Month createOrUpdateMonth(Month month) {
        return monthRepository.save(month);
    }

    public void deleteMonth(Long id) {
        monthRepository.deleteById(id);
    }
}
