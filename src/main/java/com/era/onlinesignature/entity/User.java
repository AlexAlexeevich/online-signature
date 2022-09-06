package com.era.onlinesignature.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "login")
})
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login")
    private String login;

    @JsonIgnore
    private String password;

    @Column(name = "initiator_email")
    private String initiatorEmail;

    @Column(name = "name")
    private String name;

    @Column(name = "sms_code")
    private Long smsCode;

    @Column(name = "date_code_enter")
    //@JsonIgnore
    private Date dateCodeEnter;

    @Column(name = "count_send_sms_code")
    //@JsonIgnore
    private int countSendSmsCode;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Subscriber> subscribers;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

}