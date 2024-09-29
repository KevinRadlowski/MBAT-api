package com.mbat.mbatapi.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class RecurringOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    private Date estimatedDate;

    private double amount;

    private boolean isWithdrew;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    public RecurringOperation(Date estimatedDate, double amount, boolean isWithdrew, Account account) {
        this.estimatedDate = estimatedDate;
        this.amount = amount;
        this.isWithdrew = isWithdrew;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getEstimatedDate() {
        return estimatedDate;
    }

    public void setEstimatedDate(Date estimatedDate) {
        this.estimatedDate = estimatedDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isWithdrew() {
        return isWithdrew;
    }

    public void setWithdrew(boolean isWithdrew) {
        this.isWithdrew = isWithdrew;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}