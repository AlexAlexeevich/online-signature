package com.era.onlinesignature.entity;

import com.era.onlinesignature.entity.enums.ERole;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    private ERole name;


    public Role(ERole name) {
        this.name = name;
    }
}
