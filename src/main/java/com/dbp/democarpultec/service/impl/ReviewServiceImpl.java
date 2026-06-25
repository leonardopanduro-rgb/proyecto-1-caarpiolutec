package com.dbp.democarpultec.service.impl;

import com.dbp.democarpultec.dto.ReviewRequestDto;
import com.dbp.democarpultec.dto.ReviewResponseDto;
import com.dbp.democarpultec.exception.BusinessRuleException;
import com.dbp.democarpultec.exception.DuplicateResourceException;
import com.dbp.democarpultec.exception.ForbiddenException;
import com.dbp.democarpultec.model.Review;
import com.dbp.democarpultec.model.Ride;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.model.enums.Role;
import com.dbp.democarpultec.repository.RidePassengerRepository;
import com.dbp.democarpultec.repository.ReviewRepository;
import com.dbp.democarpultec.service.ReviewService;
import com.dbp.democarpultec.service.RideService;
import com.dbp.democarpultec.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RideService rideService;
    private final UserService userService;
    private final RidePassengerRepository ridePassengerRepository;

    public List<ReviewResponseDto> findAll() {
        return reviewRepository.findAll().stream().map(this::toResponseDto).toList();
    }

    public ReviewResponseDto findById(Long id) {
        return toResponseDto(findEntityById(id));
    }

    @Transactional
    public ReviewResponseDto createAuthenticated(Long authenticatedUserId, ReviewRequestDto dto) {
        Ride ride = rideService.findEntityById(dto.getRideId());
        User reviewer = userService.findEntityById(authenticatedUserId);
        User reviewed = userService.findEntityById(dto.getReviewedId());
        validateReview(ride, reviewer, reviewed);
        if (reviewRepository.existsByRide_IdAndReviewer_IdAndReviewed_Id(
                ride.getId(), reviewer.getId(), reviewed.getId())) {
            throw new DuplicateResourceException("A review for this participant already exists in this ride");
        }

        Review review = Review.builder()
                .ride(ride)
                .reviewer(reviewer)
                .reviewed(reviewed)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();
        Review saved = reviewRepository.save(review);
        reviewRepository.flush();
        updateReviewedRating(reviewed.getId());
        return toResponseDto(saved);
    }

    @Transactional
    public ReviewResponseDto updateAuthenticated(Long id, Long authenticatedUserId, ReviewRequestDto dto) {
        Review review = findEntityById(id);
        validateReviewOwner(review, authenticatedUserId);
        if (!review.getRide().getId().equals(dto.getRideId())
                || !review.getReviewed().getId().equals(dto.getReviewedId())) {
            throw new BusinessRuleException("A review cannot change its ride or reviewed participant");
        }
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        Review saved = reviewRepository.save(review);
        reviewRepository.flush();
        updateReviewedRating(review.getReviewed().getId());
        return toResponseDto(saved);
    }

    @Transactional
    public void deleteAuthenticated(Long id, Long authenticatedUserId, Role role) {
        Review review = findEntityById(id);
        if (!review.getReviewer().getId().equals(authenticatedUserId) && role != Role.ADMIN) {
            throw new ForbiddenException("You cannot delete this review");
        }
        Long reviewedId = review.getReviewed().getId();
        reviewRepository.delete(review);
        reviewRepository.flush();
        updateReviewedRating(reviewedId);
    }

    public Review findEntityById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id " + id));
    }

    private void validateReview(Ride ride, User reviewer, User reviewed) {
        if (reviewer.getId().equals(reviewed.getId())) {
            throw new BusinessRuleException("A user cannot review themselves");
        }
        if (ride.getDepartureTime() == null || ride.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Reviews are allowed only after the ride");
        }
        if (!isParticipant(ride, reviewer) || !isParticipant(ride, reviewed)) {
            throw new BusinessRuleException("Only ride participants can be reviewed");
        }
    }

    private boolean isParticipant(Ride ride, User user) {
        return ride.getDriver().getId().equals(user.getId())
                || ridePassengerRepository.existsByRide_IdAndPassenger_Id(ride.getId(), user.getId());
    }

    private void validateReviewOwner(Review review, Long authenticatedUserId) {
        if (!review.getReviewer().getId().equals(authenticatedUserId)) {
            throw new ForbiddenException("You are not the author of this review");
        }
    }

    private void updateReviewedRating(Long reviewedId) {
        userService.updateRating(reviewedId, reviewRepository.averageRatingByReviewedId(reviewedId));
    }

    private ReviewResponseDto toResponseDto(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .rideId(review.getRide().getId())
                .reviewerId(review.getReviewer().getId())
                .reviewedId(review.getReviewed().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
