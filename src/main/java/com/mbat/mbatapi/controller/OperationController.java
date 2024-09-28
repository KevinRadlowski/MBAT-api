package com.mbat.mbatapi.controller;

import com.mbat.mbatapi.entity.Operation;
import com.mbat.mbatapi.service.OperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/operations")
public class OperationController {

    @Autowired
    private OperationService operationService;

    @GetMapping
    public List<Operation> getAllOperations() {
        return operationService.getAllOperations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Operation> getOperationById(@PathVariable Long id) {
        Optional<Operation> operation = operationService.getOperationById(id);
        return operation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Operation createOperation(@RequestBody Operation operation) {
        return operationService.createOrUpdateOperation(operation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Operation> updateOperation(@PathVariable Long id, @RequestBody Operation operation) {
        if (!operationService.getOperationById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        operation.setId(id);
        return ResponseEntity.ok(operationService.createOrUpdateOperation(operation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperation(@PathVariable Long id) {
        if (!operationService.getOperationById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        operationService.deleteOperation(id);
        return ResponseEntity.ok().build();
    }
}