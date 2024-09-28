package com.mbat.mbatapi.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Year> years;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DebitType> debitTypes;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CreditType> creditTypes;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecurringOperation> recurringOperations;

    public Account(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Year> getYears() {
        return years;
    }

    public void setYears(List<Year> years) {
        this.years = years;
    }

    public List<DebitType> getDebitTypes() {
        return debitTypes;
    }

    public void setDebitTypes(List<DebitType> debitTypes) {
        this.debitTypes = debitTypes;
    }

    public List<CreditType> getCreditTypes() {
        return creditTypes;
    }

    public void setCreditTypes(List<CreditType> creditTypes) {
        this.creditTypes = creditTypes;
    }

    public List<RecurringOperation> getRecurringOperations() {
        return recurringOperations;
    }

    public void setRecurringOperations(List<RecurringOperation> recurringOperations) {
        this.recurringOperations = recurringOperations;
    }
}