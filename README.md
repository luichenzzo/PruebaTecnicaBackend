# Optiplant Backend

This is the backend API for the Optiplant application. It is built using Spring Boot and provides the core business logic, database integration, real-time functionality via WebSockets, and secure access with JWT authentication.

## Tech Stack

- **Java 26**
- **Spring Boot 4.x**
  - Spring Web (REST API)
  - Spring Data JPA (Database access)
  - Spring Security (Authentication and Authorization)
  - Spring WebSockets (Real-time updates)
- **Database**: PostgreSQL 18
- **Authentication**: JSON Web Tokens (JJWT)
- **Containerization**: Docker & Docker Compose
- **Build Tool**: Maven
- **Boilerplate Reduction**: Lombok

## Prerequisites

Before running the application, ensure you have the following installed:

- **Java Development Kit (JDK) 26** (if running locally without Docker)
- **Maven** (optional, you can use the included `mvnw` wrapper)
- **Docker and Docker Compose** (recommended for running the database and application together)

## Running the Application

### Method 1: Using Docker Compose (Recommended)

The easiest way to get the application up and running is using Docker Compose. This will spin up both the PostgreSQL database (pre-seeded with `Database.sql`) and the Spring Boot application.

1. Navigate to the project root directory:
   ```bash
   cd PruebaTecnicaBackend
   ```

2. Run Docker Compose:
   ```bash
   docker-compose up -d --build
   ```

The API will be available at `http://localhost:8080`, and the database will be mapped to port `5433` on your host machine.

### Method 2: Running Locally (Development)

If you prefer to run the application locally for development, you can start the database via Docker and run the Spring Boot app directly.

1. Start the PostgreSQL database:
   ```bash
   docker-compose up -d optiplant-db
   ```

2. Run the Spring Boot application using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(On Windows, use `mvnw.cmd spring-boot:run`)*

## Environment Variables

The application can be configured using environment variables. These are already pre-configured for local development in `application.properties` and `docker-compose.yml`.

| Variable | Description | Default Value |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://localhost:5433/optiplant` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `root` |
| `JWT_SECRET` | Secret key for JWT signing | *(random string)* |
| `JWT_EXPIRATION_MS` | JWT token expiration time (in ms) | `86400000` (1 day) |
| `CORS_ALLOWED_ORIGINS` | Allowed origins for CORS | `http://localhost:3000` |

## Database Initialization

The database schema and initial data are managed through the `Database.sql` file located in the root directory. When you start the database container for the first time, this script is automatically executed to set up the necessary tables and populate them with default data.

The application is configured with `spring.jpa.hibernate.ddl-auto=none`, meaning Spring Boot will not attempt to create or modify the schema automatically. All database changes should be made via `Database.sql` or a proper migration tool for production.

## Features

- **User Authentication**: Secure login and session management using JWT.
- **Role-based Access Control**: Different permissions for Operators and Administrators.
- **Inventory & Branch Management**: Logic for managing inventory across multiple branches.
- **Real-Time Synchronization**: WebSockets integration (STOMP) for pushing live updates to clients (e.g., table refreshes).