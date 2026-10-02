package com.project.eliascphoto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.eliascphoto.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByIsActiveTrueOrderByIdDesc();
}
