# MultiMart E-commerce Platform - Backend

## Table of Contents
1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Setup and Installation](#setup-and-installation)
5. [Database Configuration](#database-configuration)
6. [Running the Application](#running-the-application)
7. [API Documentation](#api-documentation)
8. [Authentication Flow](#authentication-flow)
9. [Testing with Postman](#testing-with-postman)
10. [Project Flow](#project-flow)
11. [Security Implementation](#security-implementation)
12. [Error Handling](#error-handling)

## Project Overview

MultiMart is a comprehensive multi-vendor e-commerce platform that allows vendors to sell products across various categories. The platform supports three user roles: customers, vendors, and administrators. This repository contains the backend implementation built with Spring Boot.

### Key Features

- Multi-vendor marketplace
- Product catalog with categories and subcategories
- Advanced search and filtering
- Shopping cart and checkout
- User authentication and profile management
- Vendor dashboard for product management
- Admin dashboard for platform management
- Wishlist functionality
- Product reviews and ratings
- Order tracking and history

## Technology Stack

- **Framework**: Spring Boot 3.2.3
- **Language**: Java 17
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT (JSON Web Tokens)
- **Build Tool**: Maven
- **API**: RESTful API
- **Documentation**: Swagger/OpenAPI (can be added)
- **Email**: Spring Mail for notifications

## Project Structure

The project follows a standard layered architecture:

```
src/main/java/com/multimart/
├── config/                  # Configuration classes
│   ├── ApplicationConfig.java
│   ├── SecurityConfig.java
├── controller/              # REST controllers
│   ├── AdminController.java
│   ├── AuthController.java
│   ├── CartController.java
│   ├── CategoryController.java
│   ├── OrderController.java
│   ├── ProductController.java
│   ├── VendorController.java
│   ├── WishlistController.java
├── dto/                     # Data Transfer Objects
│   ├── auth/
│   ├── cart/
│   ├── category/
│   ├── common/
│   ├── order/
│   ├── product/
│   ├── user/
│   ├── vendor/
│   ├── wishlist/
├── exception/               # Custom exceptions and handlers
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
├── model/                   # Entity models
│   ├── Address.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Category.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Product.java
│   ├── Review.java
│   ├── Role.java
│   ├── Subcategory.java
│   ├── User.java
│   ├── Vendor.java
│   ├── WishlistItem.java
├── repository/              # JPA repositories
│   ├── CartItemRepository.java
│   ├── CartRepository.java
│   ├── CategoryRepository.java
│   ├── OrderRepository.java
│   ├── ProductRepository.java
│   ├── ReviewRepository.java
│   ├── SubcategoryRepository.java
│   ├── UserRepository.java
│   ├── VendorRepository.java
│   ├── WishlistItemRepository.java
├── security/                # Security components
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
├── service/                 # Business logic
│   ├── AdminService.java
│   ├── AuthService.java
│   ├── CartService.java
│   ├── CategoryService.java
│   ├── EmailService.java
│   ├── OrderService.java
│   ├── ProductService.java
│   ├── VendorService.java
│   ├── WishlistService.java
├── MultimartBackendApplication.java  # Main application class
```

## Setup and Installation

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher

### Clone the Repository

```bash
git clone https://github.com/Nikkk28/MultiMart-Ecommerce-platform-backend-using-Spring-Boot.git
cd multimart-backend