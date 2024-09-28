package com.mbat.mbatapi.controller;

import com.mbat.mbatapi.entity.CashFlow;
import com.mbat.mbatapi.service.CashFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/cashFlow")
public class CashFlowController {
    @Autowired
    private CashFlowService cashFlowService;

    @GetMapping
    public List<CashFlow> getAllCashFlow() {
        return cashFlowService.getAllCashFlow();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CashFlow> getCashFlowById(@PathVariable Long id) {
        Optional<CashFlow> cashFlow = cashFlowService.getCashFlowById(id);
        return cashFlow.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public CashFlow createCashFlow(@RequestBody CashFlow cashFlow) {
        return cashFlowService.createOrUpdateCashFlow(cashFlow);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CashFlow> updateCashFlow(@PathVariable Long id, @RequestBody CashFlow cashFlow) {
        if (cashFlowService.getCashFlowById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        cashFlow.setId(id);
        return ResponseEntity.ok(cashFlowService.createOrUpdateCashFlow(cashFlow));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCashFlow(@PathVariable Long id) {
        if (cashFlowService.getCashFlowById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        cashFlowService.deleteCashFlow(id);
        return ResponseEntity.ok().build();
    }
}
