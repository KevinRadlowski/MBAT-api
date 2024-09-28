package com.mbat.mbatapi.service;
import com.mbat.mbatapi.entity.CashFlow;
import com.mbat.mbatapi.repository.CashFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CashFlowService {

    @Autowired
    private CashFlowRepository renteeArgentRepository;

    public List<CashFlow> getAllCashFlow() {
        return renteeArgentRepository.findAll();
    }

    public Optional<CashFlow> getCashFlowById(Long id) {
        return renteeArgentRepository.findById(id);
    }

    public CashFlow createOrUpdateCashFlow(CashFlow renteeArgent) {
        return renteeArgentRepository.save(renteeArgent);
    }

    public void deleteCashFlow(Long id) {
        renteeArgentRepository.deleteById(id);
    }
}
