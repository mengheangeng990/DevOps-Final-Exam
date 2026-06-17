# ID Card Manager

A Spring Boot application for managing ID card profiles for students, employees, and users.

## Features

- CRUD operations for `Profile` entities
- File upload support for JPEG/PNG profile photos
- Live HTML preview for profile cards
- Unique UUID and registration number generation
- QR code and barcode support using ZXing
- Thymeleaf templates for profile and template UI
- MySQL database support for production

## Getting Started

1. Set up MySQL and create a database named `idcard_db`.
2. Update `src/main/resources/application.properties` with your MySQL credentials.
3. Build and run:

```bash
mvn spring-boot:run
```

4. Open the app at `http://localhost:8080/profiles`.

## Notes

- The app uses `spring.jpa.hibernate.ddl-auto=update` for schema creation.
- Uploaded photos are stored locally in the `photos` directory.
- Template management is available at `/templates`.
