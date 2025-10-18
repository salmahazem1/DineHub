package com.example.restaurant.services;

import com.example.restaurant.domain.GeoLocation;
import com.example.restaurant.domain.entities.Address;

public interface GeoLocationService {
    GeoLocation geoLocate(Address address);
}
