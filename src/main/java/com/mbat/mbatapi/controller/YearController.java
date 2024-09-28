package com.mbat.mbatapi.controller;
import com.mbat.mbatapi.entity.Year;
import com.mbat.mbatapi.service.YearService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/year")
public class YearController {
    @Autowired
    private YearService yearService;

    @GetMapping
    public List<Year> getAllYears() {
        return yearService.getAllYears();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Year> getYearById(@PathVariable Long id) {
        Optional<Year> year = yearService.getYearById(id);
        return year.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Year createYear(@RequestBody Year year) {
        return yearService.createOrUpdateYear(year);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Year> updateYear(@PathVariable Long id, @RequestBody Year year) {
        if (!yearService.getYearById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        year.setId(id);
        return ResponseEntity.ok(yearService.createOrUpdateYear(year));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteYear(@PathVariable Long id) {
        if (!yearService.getYearById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        yearService.deleteYear(id);
        return ResponseEntity.ok().build();
    }
}