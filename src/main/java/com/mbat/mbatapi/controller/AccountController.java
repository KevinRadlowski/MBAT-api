package com.mbat.mbatapi.controller;
import com.mbat.mbatapi.entity.Account;
import com.mbat.mbatapi.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        Optional<Account> account = accountService.getAccountById(id);
        return account.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        return accountService.createOrUpdateAccount(account);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id, @RequestBody Account account) {
        if (!accountService.getAccountById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        account.setId(id);
        return ResponseEntity.ok(accountService.createOrUpdateAccount(account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        if (!accountService.getAccountById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        accountService.deleteAccount(id);
        return ResponseEntity.ok().build();
    }
}
