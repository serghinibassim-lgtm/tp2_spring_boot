package com.bassim.tp2_securPharmacie.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Audited;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Audited.Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;


    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;
}