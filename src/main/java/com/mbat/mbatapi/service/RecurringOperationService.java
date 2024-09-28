package com.mbat.mbatapi.service;

import com.mbat.mbatapi.entity.RecurringOperation;
import com.mbat.mbatapi.repository.RecurringOperationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecurringOperationService {
    @Autowired
    private RecurringOperationRepository recurringOperationRepository;

    public List<RecurringOperation> getAllRecurringOperations() {
        return recurringOperationRepository.findAll();
    }

    public Optional<RecurringOperation> getRecurringOperationById(Long id) {
        return recurringOperationRepository.findById(id);
    }

    public RecurringOperation createOrUpdateRecurringOperation(RecurringOperation recurringOperation) {
        return recurringOperationRepository.save(recurringOperation);
    }

    public void deleteRecurringOperation(Long id) {
        recurringOperationRepository.deleteById(id);
    }
}