package com.mbat.mbatapi.service;

import com.mbat.mbatapi.entity.Operation;
import com.mbat.mbatapi.repository.OperationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OperationService {

    @Autowired
    private OperationRepository operationRepository;

    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

    public Optional<Operation> getOperationById(Long id) {
        return operationRepository.findById(id);
    }

    public Operation createOrUpdateOperation(Operation operation) {
        return operationRepository.save(operation);
    }

    public void deleteOperation(Long id) {
        operationRepository.deleteById(id);
    }
}
