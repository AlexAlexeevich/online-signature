package com.era.onlinesignature.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "links")
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_link_contract")
    private String nameLinkContract;

    @Column(name = "link_contract")
    private String linkContract;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;
}
