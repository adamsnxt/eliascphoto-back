package com.project.eliascphoto.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.eliascphoto.model.User;

public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByUserName(String userName);

    Optional<User> findByUserName(String userName);
}
