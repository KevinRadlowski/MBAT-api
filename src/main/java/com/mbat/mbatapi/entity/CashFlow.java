package com.mbat.mbatapi.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
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

}
