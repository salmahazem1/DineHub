package com.example.restaurant.mappers;

import com.example.restaurant.domain.RestaurantCreateUpdateRequest;
import com.example.restaurant.domain.dtos.GeoPointDto;
import com.example.restaurant.domain.dtos.RestaurantCreateUpdateRequestDto;
import com.example.restaurant.domain.dtos.RestaurantDto;
import com.example.restaurant.domain.dtos.RestaurantSummaryDto;
import com.example.restaurant.domain.entities.Restaurant;
import com.example.restaurant.domain.entities.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RestaurantMapper {
    RestaurantCreateUpdateRequest toRestaurantCreateUpdateRequest(RestaurantCreateUpdateRequestDto dto);

    @Mapping(target = "totalReviews", expression = "java(this.calculateTotalReviews(restaurant.getReviews()))")
    RestaurantDto toRestaurantDto(Restaurant restaurant);

    @Mapping(target = "latitude", expression = "java(geoPoint.getLat())")
    @Mapping(target = "longitude", expression = "java(geoPoint.getLon())")
    GeoPointDto toGeoPointDto(GeoPoint geoPoint);

    @Mapping(target = "totalReviews", expression = "java(this.calculateTotalReviews(restaurant.getReviews()))")
    RestaurantSummaryDto toSummaryDto(Restaurant restaurant);

    default Integer calculateTotalReviews(List<Review> reviews) {
        return reviews != null ? reviews.size() : 0;
    }
}
