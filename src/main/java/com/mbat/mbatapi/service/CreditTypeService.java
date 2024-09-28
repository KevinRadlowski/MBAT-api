package com.mbat.mbatapi.service;
import com.mbat.mbatapi.entity.CreditType;
import com.mbat.mbatapi.repository.CreditTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CreditTypeService {

    @Autowired
    private CreditTypeRepository creditTypeRepository;

    public List<CreditType> getAllCreditTypes() {
        return creditTypeRepository.findAll();
    }

    public Optional<CreditType> getCreditTypeById(Long id) {
        return creditTypeRepository.findById(id);
    }

    public CreditType createOrUpdateCreditType(CreditType creditType) {
        return creditTypeRepository.save(creditType);
    }

    public void deleteCreditType(Long id) {
        creditTypeRepository.deleteById(id);
    }
}
