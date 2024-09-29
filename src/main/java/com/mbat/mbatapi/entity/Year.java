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
public class Year {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @OneToMany(mappedBy = "year", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Month> month;

    public Year(int year, Account account) {
        this.year = year;
        this.account = account;
    }

}
