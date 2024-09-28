package com.mbat.mbatapi.controller;
import com.mbat.mbatapi.entity.RecurringOperation;
import com.mbat.mbatapi.service.RecurringOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recurringOperation")
public class RecurringOperationController {
    @Autowired
    private RecurringOperationService recurringOperationService;

    @GetMapping
    public List<RecurringOperation> getAllRecurringOperations() {
        return recurringOperationService.getAllRecurringOperations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringOperation> getRecurringOperationById(@PathVariable Long id) {
        Optional<RecurringOperation> recurringOperation = recurringOperationService.getRecurringOperationById(id);
        return recurringOperation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public RecurringOperation createRecurringOperation(@RequestBody RecurringOperation recurringOperation) {
        return recurringOperationService.createOrUpdateRecurringOperation(recurringOperation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringOperation> updateRecurringOperation(@PathVariable Long id, @RequestBody RecurringOperation recurringOperation) {
        if (!recurringOperationService.getRecurringOperationById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        recurringOperation.setId(id);
        return ResponseEntity.ok(recurringOperationService.createOrUpdateRecurringOperation(recurringOperation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringOperation(@PathVariable Long id) {
        if (!recurringOperationService.getRecurringOperationById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        recurringOperationService.deleteRecurringOperation(id);
        return ResponseEntity.ok().build();
    }
}