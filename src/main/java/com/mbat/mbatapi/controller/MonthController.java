package com.mbat.mbatapi.controller;
import com.mbat.mbatapi.entity.Month;
import com.mbat.mbatapi.service.MonthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/month")
public class MonthController {
    @Autowired
    private MonthService monthService;

    @GetMapping
    public List<Month> getAllMonth() {
        return monthService.getAllMonth();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Month> getMonthById(@PathVariable Long id) {
        Optional<Month> month = monthService.getMonthById(id);
        return month.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Month createMonth(@RequestBody Month month) {
        return monthService.createOrUpdateMonth(month);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Month> updateMonth(@PathVariable Long id, @RequestBody Month month) {
        if (!monthService.getMonthById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        month.setId(id);
        return ResponseEntity.ok(monthService.createOrUpdateMonth(month));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonth(@PathVariable Long id) {
        if (!monthService.getMonthById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        monthService.deleteMonth(id);
        return ResponseEntity.ok().build();
    }
}