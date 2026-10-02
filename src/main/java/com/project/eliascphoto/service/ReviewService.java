package com.project.eliascphoto.service;

import java.util.List;

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

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews() {
        return reviewRepository.findAllByIsActiveTrueOrderByIdDesc().stream()
                .map(ReviewService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReview(Long id) {
        Review review = findReview(id);
        if (!review.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reseña no encontrada");
        }
        return toResponse(review);
    }

    public ReviewResponse createReview(ReviewRequest request) {
        Review review = new Review();
        applyRequest(review, request);
        return toResponse(reviewRepository.save(review));
    }

    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        Review review = findReview(id);
        applyRequest(review, request);
        return toResponse(reviewRepository.save(review));
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
