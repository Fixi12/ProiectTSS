package ro.unibuc.hello.service;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ro.unibuc.hello.dto.review.ReviewRequestDTO;
import ro.unibuc.hello.dto.review.ReviewResponseDTO;
import ro.unibuc.hello.enums.RideStatus;
import ro.unibuc.hello.enums.RideBookingStatus;
import ro.unibuc.hello.enums.Role;
import ro.unibuc.hello.exceptions.review.InvalidReviewException;
import ro.unibuc.hello.model.Ride;
import ro.unibuc.hello.model.RideBooking;
import ro.unibuc.hello.model.Review;
import ro.unibuc.hello.model.User;
import ro.unibuc.hello.repository.RideBookingRepository;
import ro.unibuc.hello.repository.RideRepository;
import ro.unibuc.hello.repository.ReviewRepository;
import ro.unibuc.hello.repository.UserRepository;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    
    @Mock
    private RideRepository rideRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RideBookingRepository rideBookingRepository;
    
    @InjectMocks
    private ReviewService reviewService;
    
    private ReviewRequestDTO validReviewRequest;
    private ReviewRequestDTO validReviewRequest2;
    private ReviewRequestDTO validReviewRequest3;

    private Ride ride;
    private User reviewer;
    private User reviewer2;
    private User reviewer3;

    private User reviewed;
    private RideBooking rideBooking;
    private RideBooking rideBooking2;
    private RideBooking rideBooking3;
    
    @BeforeEach
    void setUp() {
        validReviewRequest = new ReviewRequestDTO();
        validReviewRequest.setReviewerId("reviewer1");
        validReviewRequest.setReviewedId("driver1");
        validReviewRequest.setRideId("ride1");
        validReviewRequest.setRating(5);
        validReviewRequest.setComment("Great ride!");

        validReviewRequest2 = new ReviewRequestDTO();
        validReviewRequest2.setReviewerId("reviewer2");
        validReviewRequest2.setReviewedId("driver1");
        validReviewRequest2.setRideId("ride1");
        validReviewRequest2.setRating(3);
        validReviewRequest2.setComment("Great ride!");

        validReviewRequest3 = new ReviewRequestDTO();
        validReviewRequest3.setReviewerId("reviewer3");
        validReviewRequest3.setReviewedId("driver1");
        validReviewRequest3.setRideId("ride1");
        validReviewRequest3.setRating(0);
        validReviewRequest3.setComment("Great ride!");

        ride = new Ride("driver1", "startLocation", "endLocation", Instant.now(), Instant.now().plusSeconds(3600), 20, 3, "XYZ123");
        ride.setStatus(RideStatus.COMPLETED);

        reviewer = new User("John", "Doe", "john@mail.com", "1234567890", Collections.singletonList(Role.PASSENGER));
        reviewer.setId("reviewer1");

        reviewer2 = new User("John", "Doe", "danutz@mail.com", "1234467890", Collections.singletonList(Role.PASSENGER));
        reviewer2.setId("reviewer2");

        reviewer3 = new User("John", "Doe", "georgi@gmail.com", "8234467890", Collections.singletonList(Role.PASSENGER));
        reviewer3.setId("reviewer3");

        reviewed = new User("Driver", "One", "driver@mail.com", "0987654321", Collections.singletonList(Role.DRIVER));
        reviewed.setId("driver1");

        rideBooking = new RideBooking("ride1", "reviewer1", Instant.now());
        rideBooking2 = new RideBooking("ride1", "reviewer2", Instant.now());
        rideBooking3 = new RideBooking("ride1", "reviewer3", Instant.now());

        rideBooking.setRideBookingStatus(RideBookingStatus.BOOKED);
        rideBooking2.setRideBookingStatus(RideBookingStatus.BOOKED);
        rideBooking3.setRideBookingStatus(RideBookingStatus.BOOKED);
    }

    @Test
    void testCreateReview_validReview() {
       
        when(userRepository.existsById(validReviewRequest.getReviewerId())).thenReturn(true);
        when(userRepository.existsById(validReviewRequest.getReviewedId())).thenReturn(true);
        when(rideRepository.findById(validReviewRequest.getRideId())).thenReturn(Optional.of(ride));
        when(rideBookingRepository.findByRideIdAndPassengerId(validReviewRequest.getRideId(), validReviewRequest.getReviewerId()))
                .thenReturn(Optional.of(rideBooking));
        when(reviewRepository.findByRideIdAndReviewerId(validReviewRequest.getRideId(), validReviewRequest.getReviewerId()))
                .thenReturn(Optional.empty());
        when(userRepository.findById(validReviewRequest.getReviewedId())).thenReturn(Optional.of(reviewed));

 
        ReviewResponseDTO response = reviewService.createReview(validReviewRequest);

      
        assertNotNull(response);
        assertEquals(validReviewRequest.getReviewerId(), response.getReviewerId());
        assertEquals(validReviewRequest.getReviewedId(), response.getReviewedId());
        assertEquals(validReviewRequest.getRideId(), response.getRideId());
        assertEquals(validReviewRequest.getRating(), response.getRating());
        assertEquals(validReviewRequest.getComment(), response.getComment());
        assertNotNull(response.getCreatedAt());

  
        verify(userRepository, times(1)).existsById(validReviewRequest.getReviewerId());
        verify(userRepository, times(1)).existsById(validReviewRequest.getReviewedId());
        verify(rideRepository, times(1)).findById(validReviewRequest.getRideId());
        verify(rideBookingRepository, times(1)).findByRideIdAndPassengerId(validReviewRequest.getRideId(), validReviewRequest.getReviewerId());
        verify(reviewRepository, times(1)).findByRideIdAndReviewerId(validReviewRequest.getRideId(), validReviewRequest.getReviewerId());
        verify(userRepository, times(1)).findById(validReviewRequest.getReviewedId());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testCreateReview_reviewerDoesNotExist() {
        when(userRepository.existsById(validReviewRequest.getReviewerId())).thenReturn(false);

    
        InvalidReviewException exception = assertThrows(InvalidReviewException.class, () -> reviewService.createReview(validReviewRequest));
        assertEquals("Reviewer does not exist as user.", exception.getMessage());
    }

    @Test
    void testCreateReview_reviewedDoesNotExist() {
        when(userRepository.existsById(validReviewRequest.getReviewerId())).thenReturn(true);
        when(userRepository.existsById(validReviewRequest.getReviewedId())).thenReturn(false);


        InvalidReviewException exception = assertThrows(InvalidReviewException.class, () -> reviewService.createReview(validReviewRequest));
        assertEquals("Reviewed does not exist as user.", exception.getMessage());
    }

    @Test
    void testCreateReview_rideDoesNotExist() {
        when(userRepository.existsById(validReviewRequest.getReviewerId())).thenReturn(true);
        when(userRepository.existsById(validReviewRequest.getReviewedId())).thenReturn(true);
        when(rideRepository.findById(validReviewRequest.getRideId())).thenReturn(Optional.empty());

     
        InvalidReviewException exception = assertThrows(InvalidReviewException.class, () -> reviewService.createReview(validReviewRequest));
        assertEquals("Ride does not exist.", exception.getMessage());
    }

    @Test
    void testCreateReview_rideNotCompleted() {
        ride.setStatus(RideStatus.SCHEDULED);

        when(userRepository.existsById(validReviewRequest.getReviewerId())).thenReturn(true);
        when(userRepository.existsById(validReviewRequest.getReviewedId())).thenReturn(true);
        when(rideRepository.findById(validReviewRequest.getRideId())).thenReturn(Optional.of(ride));

    
        InvalidReviewException exception = assertThrows(InvalidReviewException.class, () -> reviewService.createReview(validReviewRequest));
        assertEquals("Ride is not completed.", exception.getMessage());
    }

    @Test
    void testCreateReview_reviewerNotPassenger() {
        when(userRepository.existsById(validReviewRequest.getReviewerId())).thenReturn(true);
        when(userRepository.existsById(validReviewRequest.getReviewedId())).thenReturn(true);
        when(rideRepository.findById(validReviewRequest.getRideId())).thenReturn(Optional.of(ride));
        when(rideBookingRepository.findByRideIdAndPassengerId(validReviewRequest.getRideId(), validReviewRequest.getReviewerId()))
                .thenReturn(Optional.empty());

    
        InvalidReviewException exception = assertThrows(InvalidReviewException.class, () -> reviewService.createReview(validReviewRequest));
        assertEquals("Reviewer is not a passenger.", exception.getMessage());
    }

    @Test
    void testCreateReview_reviewerAlreadyReviewed() {
        when(userRepository.existsById(validReviewRequest.getReviewerId())).thenReturn(true);
        when(userRepository.existsById(validReviewRequest.getReviewedId())).thenReturn(true);
        when(rideRepository.findById(validReviewRequest.getRideId())).thenReturn(Optional.of(ride));
        when(rideBookingRepository.findByRideIdAndPassengerId(validReviewRequest.getRideId(), validReviewRequest.getReviewerId()))
                .thenReturn(Optional.of(rideBooking));
        when(reviewRepository.findByRideIdAndReviewerId(validReviewRequest.getRideId(), validReviewRequest.getReviewerId()))
                .thenReturn(Optional.of(new Review(validReviewRequest.getReviewerId(), validReviewRequest.getReviewedId(), 
                validReviewRequest.getRideId(), validReviewRequest.getRating(), validReviewRequest.getComment() )));


        InvalidReviewException exception = assertThrows(InvalidReviewException.class, () -> reviewService.createReview(validReviewRequest));
        assertEquals("Reviewer already made a review for this ride", exception.getMessage());
    }


    @Test
    void testAvgRatingIsCorrectAfterValidReview() {
        when(userRepository.existsById(validReviewRequest.getReviewerId())).thenReturn(true);
        when(userRepository.existsById(validReviewRequest.getReviewedId())).thenReturn(true);
        when(rideRepository.findById(validReviewRequest.getRideId())).thenReturn(Optional.of(ride));
        when(rideBookingRepository.findByRideIdAndPassengerId(validReviewRequest.getRideId(), validReviewRequest.getReviewerId()))
                .thenReturn(Optional.of(rideBooking));
        when(reviewRepository.findByRideIdAndReviewerId(validReviewRequest.getRideId(), validReviewRequest.getReviewerId()))
                .thenReturn(Optional.empty());
        when(userRepository.findById(validReviewRequest.getReviewedId())).thenReturn(Optional.of(reviewed));

        when(userRepository.existsById(validReviewRequest2.getReviewerId())).thenReturn(true);
        when(userRepository.existsById(validReviewRequest2.getReviewedId())).thenReturn(true);
        when(rideRepository.findById(validReviewRequest2.getRideId())).thenReturn(Optional.of(ride));
        when(rideBookingRepository.findByRideIdAndPassengerId(validReviewRequest2.getRideId(), validReviewRequest2.getReviewerId()))
                .thenReturn(Optional.of(rideBooking));
        when(reviewRepository.findByRideIdAndReviewerId(validReviewRequest2.getRideId(), validReviewRequest2.getReviewerId()))
                .thenReturn(Optional.empty());
        when(userRepository.findById(validReviewRequest2.getReviewedId())).thenReturn(Optional.of(reviewed));


        // Act: creează review-ul valid
        reviewService.createReview(validReviewRequest);
        reviewService.createReview(validReviewRequest2);

        User updatedDriver = userRepository.findById(reviewed.getId()).orElseThrow();
        assertEquals(4.0, updatedDriver.getAvgRating());
        assertEquals(8, updatedDriver.getRatingsSum());
        assertEquals(2, updatedDriver.getReviewsNumber());
    }

    @Test
    void testLessThan1Review() {
    validReviewRequest3.setRating(0);

    // Only required stubbing (for steps before the rating check)
    lenient().when(userRepository.existsById(validReviewRequest3.getReviewerId())).thenReturn(true);
    lenient().when(userRepository.existsById(validReviewRequest3.getReviewedId())).thenReturn(true);

    InvalidReviewException exception = assertThrows(InvalidReviewException.class, () -> {
        reviewService.createReview(validReviewRequest3);
    });

    assertEquals("Rating must be between 1 and 5.", exception.getMessage());
}

}
