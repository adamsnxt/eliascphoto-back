package com.project.eliascphoto.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.project.eliascphoto.model.Review;
import com.project.eliascphoto.repository.ReviewRepository;
import com.project.eliascphoto.web.dto.ReviewRequest;
import com.project.eliascphoto.web.dto.ReviewResponse;

@Service
@Transactional
public class ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews() {
        return reviewRepository.findAllByOrderByIdDesc().stream()
                .map(ReviewService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReview(Long id) {
        return toResponse(findReview(id));
    }

    public ReviewResponse createReview(ReviewRequest request) {
        Review review = new Review();
        applyRequest(review, request);
        return toResponse(reviewRepository.save(review));
    }

    public ReviewResponse updateReview(Long id, ReviewRequest request, String updateId) {
        LOGGER.info("Review update id={} review_id={} stage=lookup_started", updateId, id);
        Review review = findReview(id);
        LOGGER.info("Review update id={} review_id={} stage=found is_active_before={}",
                updateId, id, review.isActive());
        if (request.isActive() != null) {
            LOGGER.info("Review update id={} review_id={} stage=status_change_requested is_active={}",
                    updateId, id, request.isActive());
        } else {
            LOGGER.info("Review update id={} review_id={} stage=status_change_skipped reason=field_omitted",
                    updateId, id);
        }
        LOGGER.info("Review update id={} review_id={} stage=applying_changes", updateId, id);
        applyRequest(review, request);
        LOGGER.info("Review update id={} review_id={} stage=save_started is_active={}",
                updateId, id, review.isActive());
        Review savedReview = reviewRepository.save(review);
        LOGGER.info("Review update id={} review_id={} stage=saved is_active={}",
                updateId, id, savedReview.isActive());
        return toResponse(savedReview);
    }

    public void deleteReview(Long id) {
        reviewRepository.delete(findReview(id));
    }

    private Review findReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reseña no encontrada"));
    }

    private void applyRequest(Review review, ReviewRequest request) {
        review.setName(request.name().trim());
        review.setText(request.text().trim());
        review.setRate(request.rate());
        if (request.isActive() != null) {
            review.setActive(request.isActive());
        }
    }

    private static ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getName(),
                review.getText(),
                review.getRate(),
                review.isActive());
    }
}
