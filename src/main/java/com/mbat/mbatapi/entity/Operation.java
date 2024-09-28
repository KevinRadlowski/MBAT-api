package com.mbat.mbatapi.entity;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    private Date dateOperation;

    private String label;

    private double amount;

    @ManyToOne
    @JoinColumn(name = "debit_type_id")
    private DebitType debitType;

    @ManyToOne
    @JoinColumn(name = "month_id")
    private Month month;

    public Operation(Date dateOperation, String label, double amount, DebitType debitType, Month month) {
        this.dateOperation = dateOperation;
        this.label = label;
        this.amount = amount;
        this.debitType = debitType;
        this.month = month;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(Date dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public DebitType getDebitType() {
        return debitType;
    }

    public void setDebitType(DebitType debitType) {
        this.debitType = debitType;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }
}
