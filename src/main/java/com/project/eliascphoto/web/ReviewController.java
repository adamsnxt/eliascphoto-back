package com.project.eliascphoto.web;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.project.eliascphoto.service.ReviewService;
import com.project.eliascphoto.web.dto.ReviewRequest;
import com.project.eliascphoto.web.dto.ReviewResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewResponse> getReviews() {
        return reviewService.getReviews();
    }

    @GetMapping("/{id}")
    public ReviewResponse getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(request));
    }

    @PutMapping("/{id}")
    public ReviewResponse updateReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        String updateId = UUID.randomUUID().toString();
        LOGGER.info("Review update id={} review_id={} stage=request_received requested_is_active={}",
                updateId, id, request.isActive());
        try {
            ReviewResponse response = reviewService.updateReview(id, request, updateId);
            LOGGER.info("Review update id={} review_id={} stage=completed is_active={}",
                    updateId, id, response.isActive());
            return response;
        } catch (ResponseStatusException exception) {
            LOGGER.warn("Review update id={} review_id={} stage=rejected status={}",
                    updateId, id, exception.getStatusCode().value());
            throw exception;
        } catch (RuntimeException exception) {
            LOGGER.error("Review update id={} review_id={} stage=failed", updateId, id, exception);
            throw exception;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
