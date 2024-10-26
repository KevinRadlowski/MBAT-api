package com.mbat.mbatapi.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Setter
@Getter
@Entity
@Data
@NoArgsConstructor
public class CreditType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;

    private String color;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @OneToMany(mappedBy = "creditType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CashFlow> cashFlows;

    public CreditType(String label, String color, Account account) {
        this.label = label;
        this.color = color;
        this.account = account;
    }

}

