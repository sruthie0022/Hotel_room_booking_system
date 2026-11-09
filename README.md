# Hotel Room Booking System

A Spring Boot hotel booking application built with Java 21, Spring MVC, Spring Data JPA, Spring Security, Thymeleaf, and Maven. It is designed to run locally with H2 and with PostgreSQL in Docker or production.

## Project structure

```text
src/main/java/com/hotelbooking/
	config/       Security and application configuration
	controller/   MVC and REST request handlers
	dto/          Validated request and response models
	entity/       JPA domain entities
	exception/    Application exceptions and error handling
	repository/   Spring Data persistence interfaces
	service/      Booking, account, room, and payment logic
src/main/resources/
	templates/    Thymeleaf pages
	static/       CSS, JavaScript, and images
	application.properties       H2 development defaults
	application-prod.properties  PostgreSQL environment configuration
Dockerfile
compose.yaml
pom.xml
```

## Implementation stages

1. Maven, Spring Boot, and Docker foundation.
2. Database configuration and domain entities.
3. Repositories, services, DTOs, validation, and error handling.
4. Registration, login, and role-based access.
5. Customer pages and responsive hotel presentation.
6. Availability-safe booking, pricing, cancellation, and simulated payment.
7. Admin room, booking, customer, and analytics tools.
8. Automated tests and application startup verification.
9. Codespaces and Render deployment documentation.

## Run locally

Java 21 or newer and the Maven Wrapper are used. The default profile uses an in-memory H2 database.

```bash
./mvnw spring-boot:run
```

The application listens on port 8080. In Codespaces, open the forwarded port 8080 from the Ports panel; on a local machine, open the local address for port 8080.

## Run with Docker

Set a local database password in your environment, then start the application and PostgreSQL:

```bash
export DB_PASSWORD='choose-a-local-password'
docker compose up --build
```

The application is published on port 8080 by default. In Codespaces, open the forwarded application port. Uploaded room photos and database data are kept in Docker volumes. Do not reuse local credentials in production.

## Admin account

The application does not ship with a default administrator password. To seed the initial admin account, provide both values before the first startup:

```bash
export HOTEL_ADMIN_EMAIL='admin@example.com'
export HOTEL_ADMIN_PASSWORD='use-a-unique-strong-password'
```

For `docker compose`, these values are forwarded to the app container. Customer self-registration never grants admin access.

## Render deployment

1. Create a Render PostgreSQL database and a Web Service from this repository using the Docker runtime.
2. Set `SPRING_PROFILES_ACTIVE=prod` and `PORT` to the port assigned by Render (Render normally supplies `PORT`).
3. Configure `DB_URL` as a JDBC URL, for example `jdbc:postgresql://<host>:5432/<database>` using the Render database's internal connection details. Set `DB_USERNAME` and `DB_PASSWORD` from Render's database credentials.
4. Set `HOTEL_ADMIN_EMAIL` and `HOTEL_ADMIN_PASSWORD` as secret environment variables if an admin account should be seeded at startup. Never commit these values.
5. Set `UPLOAD_DIR` to a mounted persistent disk path if room images must survive container replacement. Without a persistent disk, database content persists but locally uploaded images do not.

Flyway applies the database schema on startup and Hibernate validates it. The app binds to the platform-provided port. The production profile disables the H2 console and enables secure session cookies.

## GitHub Codespaces

Open the repository in a Codespace, then run `./mvnw spring-boot:run` and forward port 8080. For a PostgreSQL-backed Codespace, start Docker Compose with `DB_PASSWORD` set. The default local profile uses H2 and needs no database setup.

## Test and package

```bash
./mvnw test
./mvnw package
```