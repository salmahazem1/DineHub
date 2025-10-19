# DineHub

A modern web application built with Spring Boot, Elasticsearch, and Keycloak that allows users to discover, review, and rate restaurants. Users can share detailed dining experiences, browse reviews from other users, search restaurants by location and cuisine type, and make informed decisions about where to eat.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Key Features Explained](#key-features-explained)
- [Development](#development)

## Features

### User Management
- OAuth2 authentication with Keycloak
- Secure JWT-based authorization
- User profile management with Keycloak integration

### Restaurant Management
- Create, read, update, and delete restaurants
- Store comprehensive restaurant information (name, cuisine type, contact info)
- Address management with street, city, postal code, etc.
- Operating hours tracking for each day of the week
- Geolocation-based search (latitude/longitude)
- Full-text search by restaurant name and cuisine type

### Review System
- Write detailed reviews with ratings (1-5 stars)
- Upload photos with reviews
- Edit reviews within 48 hours of posting
- Delete reviews (owner only)
- Prevent duplicate reviews (one review per user per restaurant)
- Track review metadata (creation date, last edited date)
- Calculate and display average restaurant ratings

### Search & Filtering
- Full-text search powered by Elasticsearch
- Filter restaurants by minimum rating
- Geolocation-based proximity search with radius
- Fuzzy matching for restaurant names and cuisine types
- Pagination support for search results

### Photo Management
- Upload restaurant and review photos
- File storage on local filesystem
- Photo retrieval and serving
- URL-based photo references

### Error Handling
- Comprehensive exception handling
- Validation for all inputs
- Meaningful error messages

## Tech Stack

### Backend
- **Framework:** Spring Boot 3.5.3
- **Language:** Java 21
- **Search Engine:** Elasticsearch 8.12.0
- **Authentication:** Keycloak 23.0 with OAuth2
- **Data Mapping:** MapStruct 1.6.3
- **Project Lombok:** 1.18.36 (reduces boilerplate code)

### Infrastructure
- **Docker:** Docker Compose for containerization
- **Search & Analytics:** Kibana 8.12.0 (Elasticsearch UI)
- **Security:** Spring Security with OAuth2 Resource Server

### Build Tools
- **Build System:** Maven
- **Dependency Management:** Spring Boot Parent POM

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java:** JDK 21 or higher
- **Maven:** 3.8.0 or higher
- **Docker:** Latest version
- **Docker Compose:** 2.0 or higher
- **Git:** For cloning the repository

## Installation

### Step 1: Clone the Repository

```bash
git clone https://github.com/salmahazem1/DineHub.git
cd DineHub
```

### Step 2: Start Infrastructure with Docker Compose

The project includes a `docker-compose.yaml` file that sets up Elasticsearch, Kibana, and Keycloak.

```bash
docker-compose up -d
```

This will start:
- **Elasticsearch:** Available at `http://localhost:9200`
- **Kibana:** Available at `http://localhost:5601` (for monitoring and indexing)
- **Keycloak:** Available at `http://localhost:9090` (for authentication)

### Step 3: Configure Keycloak

1. Open Keycloak at `http://localhost:9090`
2. Login with:
   - **Username:** admin
   - **Password:** admin
3. Create a new realm called `restaurant-reviews`
4. Create a client for OAuth2 authentication
5. Configure redirect URIs and valid post logout redirect URIs

### Step 4: Build and Run the Application

```bash
# Navigate to project root
cd Restaurant-review-platform

# Build the project with Maven
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Configuration

### Application Properties

Configure your application by editing `src/main/resources/application.properties`:

```properties
# Application Name
spring.application.name=restaurant

# Elasticsearch Configuration
spring.elasticsearch.uris=http://localhost:9200

# OAuth2 Resource Server (Keycloak)
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/restaurant-reviews

# File Storage Location (Update this path based on your system)
app.storage.location=C:/Users/YourUsername/Downloads/restaurant/storage
```

**Important:** Update the storage location to a valid path on your system where restaurant photos will be stored.

### Security Configuration

The application uses Spring Security with JWT authentication. Endpoints are configured as follows:

- **Public Endpoints:**
  - `GET /api/restaurants/**` - Browse restaurants
  - `GET /api/photos/**` - View photos

- **Protected Endpoints:**
  - All `POST`, `PUT`, `DELETE` operations require authentication
  - User information is extracted from JWT tokens

## Running the Application

### Option 1: Using Maven

```bash
mvn spring-boot:run
```

### Option 2: Using Compiled JAR

```bash
mvn clean package
java -jar target/restaurant-0.0.1-SNAPSHOT.jar
```

### Option 3: Using Docker (Optional)

Create a `Dockerfile` in the project root:

```dockerfile
FROM openjdk:21-slim
COPY target/restaurant-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Then:

```bash
docker build -t restaurant-review-app .
docker run -p 8080:8080 --network restaurant-network restaurant-review-app
```

## API Documentation

### Restaurants Endpoints

#### Create Restaurant
```http
POST /api/restaurants
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Italian Restaurant",
  "cuisineType": "Italian",
  "contactInformation": "contact@restaurant.com",
  "address": {
    "streetNumber": "123",
    "streetName": "Main St",
    "city": "London",
    "state": "England",
    "postalCode": "SW1A 1AA",
    "country": "UK"
  },
  "operatingHours": {
    "monday": {"openTime": "09:00", "closeTime": "22:00"},
    "tuesday": {"openTime": "09:00", "closeTime": "22:00"},
    ...
  },
  "photoIds": ["photo-id-1", "photo-id-2"]
}
```

#### Search Restaurants
```http
GET /api/restaurants?q=italian&minRating=4.0&page=1&size=20
```

Query Parameters:
- `q` - Search query (restaurant name or cuisine type)
- `minRating` - Minimum average rating filter
- `latitude` - Latitude for location-based search
- `longitude` - Longitude for location-based search
- `radius` - Search radius in km
- `page` - Page number (1-indexed)
- `size` - Results per page

#### Get Restaurant Details
```http
GET /api/restaurants/{restaurantId}
```

#### Update Restaurant
```http
PUT /api/restaurants/{restaurantId}
Authorization: Bearer <token>
Content-Type: application/json
```

#### Delete Restaurant
```http
DELETE /api/restaurants/{restaurantId}
Authorization: Bearer <token>
```

### Reviews Endpoints

#### Create Review
```http
POST /api/restaurants/{restaurantId}/reviews
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "Great food and excellent service!",
  "rating": 5,
  "photoIds": ["photo-id-1"]
}
```

#### Get Restaurant Reviews
```http
GET /api/restaurants/{restaurantId}/reviews?page=1&size=20&sort=datePosted,desc
```

#### Get Specific Review
```http
GET /api/restaurants/{restaurantId}/reviews/{reviewId}
```

#### Update Review
```http
PUT /api/restaurants/{restaurantId}/reviews/{reviewId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "Updated review text",
  "rating": 4,
  "photoIds": ["photo-id-1"]
}
```

#### Delete Review
```http
DELETE /api/restaurants/{restaurantId}/reviews/{reviewId}
Authorization: Bearer <token>
```

### Photos Endpoints

#### Upload Photo
```http
POST /api/photos
Content-Type: multipart/form-data

file: <binary-file>
```

Response:
```json
{
  "url": "filename-uuid.jpg",
  "uploadDate": "2024-01-15T10:30:00"
}
```

#### Get Photo
```http
GET /api/photos/{photoId}
```

## Project Structure

```
Restaurant-review-platform/
├── src/
│   ├── main/
│   │   ├── java/com/example/restaurant/
│   │   │   ├── RestaurantApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── JpaConfiguration.java
│   │   │   ├── controllers/
│   │   │   │   ├── RestaurantController.java
│   │   │   │   ├── ReviewController.java
│   │   │   │   ├── PhotoController.java
│   │   │   │   └── ErrorController.java
│   │   │   ├── services/
│   │   │   │   ├── RestaurantService.java
│   │   │   │   ├── ReviewService.java
│   │   │   │   ├── PhotoService.java
│   │   │   │   └── impl/
│   │   │   ├── repositories/
│   │   │   │   └── RestaurantRepository.java
│   │   │   ├── domain/
│   │   │   │   ├── entities/
│   │   │   │   │   ├── Restaurant.java
│   │   │   │   │   ├── Review.java
│   │   │   │   │   ├── Photo.java
│   │   │   │   │   └── User.java
│   │   │   │   └── dtos/
│   │   │   │       ├── RestaurantDto.java
│   │   │   │       ├── ReviewDto.java
│   │   │   │       └── PhotoDto.java
│   │   │   ├── mappers/
│   │   │   │   ├── RestaurantMapper.java
│   │   │   │   └── ReviewMapper.java
│   │   │   └── exceptions/
│   │   │       ├── RestaurantNotFoundException.java
│   │   │       ├── ReviewNotAllowedException.java
│   │   │       └── StorageException.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/example/restaurant/RestaurantApplicationTests.java
├── docker-compose.yaml
├── pom.xml
└── README.md
```

## Key Features Explained

### Elasticsearch Integration

The application uses Elasticsearch for powerful full-text search capabilities:

- Fuzzy matching for restaurant names and cuisine types
- Geolocation-based proximity queries
- Rating-based filtering
- Complex boolean queries combining multiple criteria

### Review Constraints

- **One review per user per restaurant:** Prevents duplicate reviews from the same user
- **48-hour edit window:** Users can only edit reviews within 48 hours of posting
- **Owner-only deletion:** Only the review author can delete their review

### Geolocation Search

The application includes a `RandomLondonGeoLocationService` that generates random coordinates within London boundaries:
- Latitude: 51.28 to 51.686
- Longitude: -0.489 to 0.236

For production, integrate with a real geolocation service.

### File Storage

Photos are stored on the local filesystem. Configure the storage location in `application.properties`:

```properties
app.storage.location=C:/path/to/storage
```

Ensure the directory exists or the application will create it on startup.

## Development

### Building the Project

```bash
# Clean build
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run tests
mvn test
```

### Code Style

- Follow Java naming conventions
- Use Lombok annotations to reduce boilerplate
- Leverage MapStruct for entity-to-DTO mapping

### Adding Dependencies

Edit `pom.xml` and add dependencies, then rebuild:

```bash
mvn clean install
``` 



