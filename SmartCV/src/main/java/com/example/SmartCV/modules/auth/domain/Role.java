package com.example.SmartCV.modules.auth.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Setter
@Getter
public class Role {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

}
