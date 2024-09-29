package com.mbat.mbatapi.entity;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
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

}
