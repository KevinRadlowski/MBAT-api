package com.mbat.mbatapi.service;

import com.mbat.mbatapi.entity.DebitType;
import com.mbat.mbatapi.repository.DebitTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DebitTypeService {

    @Autowired
    private DebitTypeRepository debitTypeRepository;

    public List<DebitType> getAllDebitTypes() {
        return debitTypeRepository.findAll();
    }

    public Optional<DebitType> getDebitTypeById(Long id) {
        return debitTypeRepository.findById(id);
    }

    public DebitType createOrUpdateDebitType(DebitType debitType) {
        return debitTypeRepository.save(debitType);
    }

    public void deleteDebitType(Long id) {
        debitTypeRepository.deleteById(id);
    }
}