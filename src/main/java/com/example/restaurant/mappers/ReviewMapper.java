package com.example.restaurant.mappers;

import com.example.restaurant.domain.ReviewCreateUpdateRequest;
import com.example.restaurant.domain.dtos.ReviewCreateUpdateRequestDto;
import com.example.restaurant.domain.dtos.ReviewDto;
import com.example.restaurant.domain.entities.Review;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    ReviewCreateUpdateRequest toReviewCreateUpdateRequest(ReviewCreateUpdateRequestDto dto);

    ReviewDto toDto(Review review);
}