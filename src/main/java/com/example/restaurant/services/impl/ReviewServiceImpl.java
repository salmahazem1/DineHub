package com.example.restaurant.services.impl;

import com.example.restaurant.domain.ReviewCreateUpdateRequest;
import com.example.restaurant.domain.entities.Photo;
import com.example.restaurant.domain.entities.Restaurant;
import com.example.restaurant.domain.entities.Review;
import com.example.restaurant.domain.entities.User;
import com.example.restaurant.exceptions.RestaurantNotFoundException;
import com.example.restaurant.exceptions.ReviewNotAllowedException;
import com.example.restaurant.repositories.RestaurantRepository;
import com.example.restaurant.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final RestaurantRepository restaurantRepository;

    @Override
    public Review createReview(User author, String restaurantId, ReviewCreateUpdateRequest createReview) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);

        boolean hasExistingReview = restaurant.getReviews().stream()
                .anyMatch(r -> r.getWrittenBy().getId().equals(author.getId()));
        if (hasExistingReview) {
            throw new ReviewNotAllowedException("User has already reviewed this restaurant");
        }
        LocalDateTime now = LocalDateTime.now();

        List<Photo> photos = createReview.getPhotoIds().stream().map(url -> {
            Photo photo = new Photo();
            photo.setUrl(url);
            photo.setUploadDate(now);
            return photo;
        }).collect(Collectors.toList());

        Review review = Review.builder()
                .id(UUID.randomUUID().toString())
                .content(createReview.getContent())
                .rating(createReview.getRating())
                .photos(photos)
                .datePosted(now)
                .lastEdited(now)
                .writtenBy(author)
                .build();

        restaurant.getReviews().add(review);

        updateRestaurantAverageRating(restaurant);

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);

        return updatedRestaurant.getReviews().stream()
                .filter(r -> r.getDatePosted().equals(review.getDatePosted()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error retrieving created review"));
    }

    private Restaurant getRestaurantOrThrow(String restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with id: " + restaurantId));
    }

    private void updateRestaurantAverageRating(Restaurant restaurant) {
        List<Review> reviews = restaurant.getReviews();
        if (reviews.isEmpty()) {
            restaurant.setAverageRating(0.0f);
        } else {
            float averageRating = (float) reviews.stream()
                    .mapToDouble(Review::getRating)
                    .average()
                    .orElse(0.0);
            restaurant.setAverageRating(averageRating);
        }
    }

    @Override
    public Page<Review> listReviews(String restaurantId, Pageable pageable) {

        Restaurant restaurant = getRestaurantOrThrow(restaurantId);

        List<Review> reviews = new ArrayList<>(restaurant.getReviews());

        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Sort.Order order = sort.iterator().next();
            String property = order.getProperty();
            boolean isAscending = order.getDirection().isAscending();
            Comparator<Review> comparator = switch (property) {
                case "datePosted" -> Comparator.comparing(Review::getDatePosted);
                case "rating" -> Comparator.comparing(Review::getRating);
                default -> Comparator.comparing(Review::getDatePosted);
            };
            reviews.sort(isAscending ? comparator : comparator.reversed());
        } else {

            reviews.sort(Comparator.comparing(Review::getDatePosted).reversed());
        }

        int start = (int) pageable.getOffset();

        if (start >= reviews.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, reviews.size());
        }
        int end = Math.min((start + pageable.getPageSize()), reviews.size());

        return new PageImpl<>(reviews.subList(start, end), pageable, reviews.size());
    }

    @Override
    public Optional<Review> getRestaurantReview(String restaurantId, String reviewId) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        return restaurant.getReviews().stream()
                .filter(r -> reviewId.equals(r.getId()))
                .findFirst();
    }

    @Override
    public Review updateReview(User user, String restaurantId, String reviewId,
                               ReviewCreateUpdateRequest updatedReview) {

        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        String currentUserId = user.getId();

        List<Review> reviews = restaurant.getReviews();
        Review existingReview = reviews.stream()
                .filter(r -> r.getId().equals(reviewId) &&
                        r.getWrittenBy().getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (LocalDateTime.now().isAfter(existingReview.getDatePosted().plusHours(48))) {
            throw new ReviewNotAllowedException("Review can no longer be edited (48-hour limit exceeded)");
        }

        existingReview.setContent(updatedReview.getContent());
        existingReview.setRating(updatedReview.getRating());
        existingReview.setLastEdited(LocalDateTime.now());

        existingReview.setPhotos(updatedReview.getPhotoIds().stream()
                .map(url -> {
                    Photo photo = new Photo();
                    photo.setUrl(url);
                    photo.setUploadDate(LocalDateTime.now());
                    return photo;
                }).collect(Collectors.toList()));

        updateRestaurantAverageRating(restaurant);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        return savedRestaurant.getReviews().stream()
                .filter(r -> r.getId().equals(reviewId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error retrieving updated review"));
    }

    @Override
    public void deleteReview(String restaurantId, String reviewId) {

        Restaurant restaurant = getRestaurantOrThrow(restaurantId);

        List<Review> filteredReviews = restaurant.getReviews().stream()
                .filter(review -> !reviewId.equals(review.getId()))
                .toList();

        restaurant.setReviews(filteredReviews);

        updateRestaurantAverageRating(restaurant);

        restaurantRepository.save(restaurant);
    }

}
