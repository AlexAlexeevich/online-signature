package com.era.onlinesignature.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_contract")
    private String nameContract;

    @Column(name = "short_url")
    private String shortUrl;

    @Column(name = "date_create_short_url")
    //@JsonIgnore
    private Date dateCreateShortUrl;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "subscriber_id", nullable = false)
    private Long subscriberId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "contract_id")
    private List<Link> links;
}