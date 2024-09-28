package com.mbat.mbatapi.controller;

import com.mbat.mbatapi.entity.CreditType;
import com.mbat.mbatapi.service.CreditTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/creditType")
public class CreditTypeController {
    @Autowired
    private CreditTypeService creditTypeService;

    @GetMapping
    public List<CreditType> getAllCreditTypes() {
        return creditTypeService.getAllCreditTypes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditType> getCreditTypeById(@PathVariable Long id) {
        Optional<CreditType> creditType = creditTypeService.getCreditTypeById(id);
        return creditType.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public CreditType createCreditType(@RequestBody CreditType creditType) {
        return creditTypeService.createOrUpdateCreditType(creditType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditType> updateCreditType(@PathVariable Long id, @RequestBody CreditType creditType) {
        if (!creditTypeService.getCreditTypeById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        creditType.setId(id);
        return ResponseEntity.ok(creditTypeService.createOrUpdateCreditType(creditType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCreditType(@PathVariable Long id) {
        if (!creditTypeService.getCreditTypeById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        creditTypeService.deleteCreditType(id);
        return ResponseEntity.ok().build();
    }
}
