package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.ReviewRequestDto;
import com.dbp.democarpultec.dto.ReviewResponseDto;
import com.dbp.democarpultec.exception.BusinessRuleException;
import com.dbp.democarpultec.exception.ForbiddenException;
import com.dbp.democarpultec.model.Review;
import com.dbp.democarpultec.model.Ride;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.model.enums.Role;
import com.dbp.democarpultec.repository.ReviewRepository;
import com.dbp.democarpultec.repository.RidePassengerRepository;
import com.dbp.democarpultec.service.impl.ReviewServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private RideService rideService;
    @Mock
    private UserService userService;
    @Mock
    private RidePassengerRepository ridePassengerRepository;
    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void shouldCreateReviewFromAuthenticatedParticipantAndRecalculateRating() {
        User reviewer = user(2L);
        User reviewed = user(3L);
        Ride ride = completedRide(reviewer);
        ReviewRequestDto dto = request(3L, 5);

        when(rideService.findEntityById(1L)).thenReturn(ride);
        when(userService.findEntityById(2L)).thenReturn(reviewer);
        when(userService.findEntityById(3L)).thenReturn(reviewed);
        when(ridePassengerRepository.existsByRide_IdAndPassenger_Id(1L, 3L)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(1L);
            review.setCreatedAt(LocalDateTime.now());
            return review;
        });
        when(reviewRepository.averageRatingByReviewedId(3L)).thenReturn(5.0);

        ReviewResponseDto result = reviewService.createAuthenticated(2L, dto);

        assertEquals(2L, result.getReviewerId());
        assertEquals(3L, result.getReviewedId());
        verify(userService).updateRating(3L, 5.0);
    }

    @Test
    void shouldRejectReviewWhenReviewedUserDidNotParticipateInRide() {
        User reviewer = user(2L);
        Ride ride = completedRide(reviewer);
        when(rideService.findEntityById(1L)).thenReturn(ride);
        when(userService.findEntityById(2L)).thenReturn(reviewer);
        when(userService.findEntityById(3L)).thenReturn(user(3L));

        assertThrows(BusinessRuleException.class,
                () -> reviewService.createAuthenticated(2L, request(3L, 5)));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldRejectReviewBeforeRideDeparture() {
        User reviewer = user(2L);
        Ride ride = completedRide(reviewer);
        ride.setDepartureTime(LocalDateTime.now().plusHours(1));
        when(rideService.findEntityById(1L)).thenReturn(ride);
        when(userService.findEntityById(2L)).thenReturn(reviewer);
        when(userService.findEntityById(3L)).thenReturn(user(3L));

        assertThrows(BusinessRuleException.class,
                () -> reviewService.createAuthenticated(2L, request(3L, 5)));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldUpdateOwnReviewAndRecalculateRating() {
        User reviewer = user(2L);
        User reviewed = user(3L);
        Review review = review(reviewer, reviewed);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.averageRatingByReviewedId(3L)).thenReturn(4.0);

        ReviewResponseDto result = reviewService.updateAuthenticated(1L, 2L, request(3L, 4));

        assertEquals(4, result.getRating());
        verify(userService).updateRating(3L, 4.0);
    }

    @Test
    void shouldRejectUpdateByAnotherUser() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review(user(2L), user(3L))));

        assertThrows(ForbiddenException.class,
                () -> reviewService.updateAuthenticated(1L, 9L, request(3L, 4)));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldAllowAdminToDeleteReviewAndRecalculateRating() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review(user(2L), user(3L))));
        when(reviewRepository.averageRatingByReviewedId(3L)).thenReturn(null);

        reviewService.deleteAuthenticated(1L, 99L, Role.ADMIN);

        verify(reviewRepository).delete(any(Review.class));
        verify(userService).updateRating(3L, null);
    }

    @Test
    void shouldReturnReviewWhenIdExists() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review(user(2L), user(3L))));

        ReviewResponseDto result = reviewService.findById(1L);

        assertEquals(2L, result.getReviewerId());
    }

    @Test
    void shouldThrowExceptionWhenReviewNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> reviewService.findById(99L));
    }

    private ReviewRequestDto request(Long reviewedId, int rating) {
        return ReviewRequestDto.builder()
                .rideId(1L)
                .reviewedId(reviewedId)
                .rating(rating)
                .comment("Buen viaje")
                .build();
    }

    private Ride completedRide(User driver) {
        Ride ride = new Ride();
        ride.setId(1L);
        ride.setDriver(driver);
        ride.setDepartureTime(LocalDateTime.now().minusHours(1));
        return ride;
    }

    private Review review(User reviewer, User reviewed) {
        Ride ride = completedRide(reviewer);
        Review review = new Review();
        review.setId(1L);
        review.setRide(ride);
        review.setReviewer(reviewer);
        review.setReviewed(reviewed);
        review.setRating(5);
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
