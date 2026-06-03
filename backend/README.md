# BCS Network Monitor — Backend

Spring Boot REST API for registering network devices and tracking their operational status. Exposes 4 endpoints for device registration, listing, detail retrieval, and status report submission.

## Prerequisites

- **Java 26** — required (Lombok 1.18.46 is not compatible with JDK 27+)
- **Docker** — for running PostgreSQL via Docker Compose
- **Maven** — the project includes a Maven wrapper (`./mvnw`), no separate installation needed

## Project Structure

```
src/main/java/com/bcs/networkmonitor/
├── config/          WebConfig (CORS), AppProperties (@ConfigurationProperties)
├── controller/      DeviceController (REST endpoints)
├── dto/             Request/response records
├── entity/          JPA entities and enums
├── exception/       Custom exceptions + GlobalExceptionHandler
├── repository/      Spring Data JPA repositories
└── service/         DeviceService + DeviceServiceImpl, StatusReportService + StatusReportServiceImpl
```

```
src/main/resources/
├── application.yaml
└── db/migration/
    └── V1__init.sql
```

## Setup

### 1. Start PostgreSQL

```bash
docker compose up -d
```

This starts a PostgreSQL container with:
- Database: `network_monitor`
- User: `admin`
- Password: `secret`
- Port: `5432`


### 2. Environment Variables

The application uses these defaults (set in `application.yaml`), which you can override:

| Variable                | Default   | Description                                |
|-------------------------|-----------|--------------------------------------------|
| `DB_USER`               | `admin`   | PostgreSQL username                        |
| `DB_PASS`               | `secret`  | PostgreSQL password                        |
| `APP_STALE_THRESHOLD`   | `15m`     | Stale threshold (e.g., `15m`, `1h`, `30s`) |

```bash
export DB_USER=admin
export DB_PASS=secret
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

Flyway will automatically run the `V1__init.sql` migration on startup, creating the `devices` and `status_reports` tables.

The API will be available for the frontend at **http://localhost:8080/api**.

## API Documentation

Interactive Swagger UI is available at **http://localhost:8080/swagger-ui.html** when the application is running.

| URL | Description |
|-----|-------------|
| `/swagger-ui.html` | Interactive Swagger UI |
| `/v3/api-docs` | OpenAPI JSON spec |

## Running Tests

```bash
./mvnw test
```

The test suite includes:
- **Unit tests** — Service layer with Mockito mocks (`DeviceServiceTest`, `StatusReportServiceTest`)
- **Controller tests** — `@WebMvcTest` with MockMvc (`DeviceControllerTest`)
- **E2E tests** — `@SpringBootTest` with `TestRestTemplate` and Testcontainers (`DeviceControllerE2ETest`)
- **Repository integration tests** — `@DataJpaTest` with Testcontainers (`DeviceRepositoryIntegrationTest`)

All 26 tests run automatically with `./mvnw test` (Testcontainers requires Docker).

## API Endpoints

| Method | Endpoint                          | Request Body               | Response                  | Status |
|--------|-----------------------------------|----------------------------|---------------------------|--------|
| POST   | `/api/devices`                    | `DeviceRegistrationRequest`| `Device`                  | 201    |
| GET    | `/api/devices`                    | —                          | `DeviceListItemResponse[]`| 200    |
| GET    | `/api/devices/{id}`               | —                          | `DeviceDetailResponse`    | 200    |
| POST   | `/api/devices/{id}/status-reports`| `StatusReportRequest`      | `StatusReportResponse`    | 201    |

### Error Responses

All errors return:

```json
{
  "message": "string",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

| Status | Condition                      |
|--------|--------------------------------|
| 400    | Validation error               |
| 404    | Device not found               |
| 409    | Duplicate `uniqueId`           |