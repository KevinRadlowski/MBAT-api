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
public class Month {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "year_id")
    private Year year;

    @OneToMany(mappedBy = "month", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Operation> operations;

    public Month(String name, Year year) {
        this.name = name;
        this.year = year;
    }

}

