package com.mbat.mbatapi.controller;
import com.mbat.mbatapi.entity.DebitType;
import com.mbat.mbatapi.service.DebitTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/debitType")
public class DebitTypeController {
    @Autowired
    private DebitTypeService debitTypeService;

    @GetMapping
    public List<DebitType> getAllDebitTypes() {
        return debitTypeService.getAllDebitTypes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DebitType> getDebitTypeById(@PathVariable Long id) {
        Optional<DebitType> debitType = debitTypeService.getDebitTypeById(id);
        return debitType.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public DebitType createDebitType(@RequestBody DebitType debitType) {
        return debitTypeService.createOrUpdateDebitType(debitType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DebitType> updateDebitType(@PathVariable Long id, @RequestBody DebitType debitType) {
        if (!debitTypeService.getDebitTypeById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        debitType.setId(id);
        return ResponseEntity.ok(debitTypeService.createOrUpdateDebitType(debitType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDebitType(@PathVariable Long id) {
        if (!debitTypeService.getDebitTypeById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        debitTypeService.deleteDebitType(id);
        return ResponseEntity.ok().build();
    }
}