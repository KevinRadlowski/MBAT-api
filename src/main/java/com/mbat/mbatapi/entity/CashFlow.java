package com.mbat.mbatapi.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class CashFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;

    @ManyToOne
    @JoinColumn(name = "type_rentree_id")
    private CreditType creditType;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    public CashFlow(double amount, CreditType creditType, Account account) {
        this.amount = amount;
        this.creditType = creditType;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public CreditType getTypeRentree() {
        return creditType;
    }

    public void setTypeRentree(CreditType creditType) {
        this.creditType = creditType;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
