package com.project.eliascphoto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;
}
