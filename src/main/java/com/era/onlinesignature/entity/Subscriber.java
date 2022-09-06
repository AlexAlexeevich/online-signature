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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "subscribers")
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscriber_email")
    private String subscriberEmail;

    @Column(name = "subscriber_phone")
    private String subscriberPhone;

    @Column(name = "sms_code")
    private Long smsCode;

    @Column(name = "date_code_enter")
    //@JsonIgnore
    private Date dateCodeEnter;

    @Column(name = "count_send_sms_code")
    //@JsonIgnore
    private int countSendSmsCode;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "subscriber_id")
    private List<Contract> contracts;
}
