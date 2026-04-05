# RK Scientific Invoice Management System

![Java](https://img.shields.io/badge/Java-25-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.5-6DB33F.svg?logo=spring-boot)
![Vaadin](https://img.shields.io/badge/Vaadin-25.1.1-00B4F0.svg?logo=vaadin)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-316192.svg?logo=postgresql)

A modern Spring Boot application designed to manage business workflows: Customers, Products, and Invoices. It features a rich, responsive web interface built with Vaadin.

The home route (`/`) opens an admin dashboard with three monetary KPI cards (This Month Invoice Value, Total Invoice Value, and Average Invoice Value), quick links into the main workflows, and a Recent Invoices section.

## 🛠 Tech Stack
- **Core**: Java 25, Spring Boot 4.0.0
- **UI Framework**: Vaadin 25.1.0 (Server-side, Java-based components)
- **Data Access**: Spring Data JDBC
- **Database**: PostgreSQL (managed via Docker Compose & Flyway migrations)

## 🚀 Getting Started

### Prerequisites
- **Java 25** installed
- **Docker** and **Docker Compose** installed (Used for automatically spinning up the PostgreSQL database during development and testing).

### Running the Application Locally

The project is configured to automatically start a PostgreSQL database using Docker Compose when you run the application.

```bash
# Start the application (compiles frontend, starts DB, and starts the Spring Boot server)
./gradlew bootRun
```
Access the application at: `http://localhost:8080/`

Actuator monitoring endpoints available at:
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/flyway`

### Running with Demo Profile (Mock Data)

The application supports a `demo` profile that automatically generates mock data on startup to help you test the UI.

**Default Configuration:**
Loads 10 customers, 10 products, and 5 invoices automatically.
```bash
./gradlew bootRun --args='--spring.profiles.active=demo'
```

**Custom Number of Mock Records:**
```bash
./gradlew bootRun --args='--spring.profiles.active=demo --invoice.customers=50 --invoice.products=20 --invoice.invoices=15'
```

**Using Environment Variables:**
```bash
SPRING_PROFILES_ACTIVE=demo INVOICE_CUSTOMERS=25 INVOICE_PRODUCTS=10 ./gradlew bootRun
```

## 🛠 Useful Commands

| Gradle Command             | Description                                   |
|:---------------------------|:----------------------------------------------|
| `./gradlew bootRun`        | Run the application locally.                  |
| `./gradlew build`          | Build the application and run all tests.      |
| `./gradlew test`           | Run unit and integration tests.               |
| `./gradlew bootJar`        | Package the application as an executable JAR. |

## 🗄 Database Management & Commands

The database schema is managed via Flyway (`src/main/resources/db/migration/*.sql`). The local Postgres container is automatically managed by Spring Boot Docker Compose, but if you need to manually connect to or inspect it:

Start an interactive PSQL console inside the container:
```bash
docker exec -it invoice-postgres psql -U user -d invoice_db
```

Access PostgreSQL directly from your host machine:
```bash
PGPASSWORD=secret psql -h localhost -p 5433 -U user -d invoice_db
```

### Useful PSQL Commands

| PSQL Command               | Description                                    |
|:---------------------------|:-----------------------------------------------|
| `\list`                    | List all databases.                            |
| `\connect invoice_db`      | Connect to specific database.                  |
| `\dt`                      | List all tables.                               |
| `\d customers`             | Show the `customers` table schema.             |
| `\d flyway_schema_history` | Show the `flyway_schema_history` table schema. |
| `\quit`                    | Quit interactive psql console.                 |

To see your Flyway migration history inside the database:
```sql
SELECT * FROM flyway_schema_history;
```
