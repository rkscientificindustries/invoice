## 🚀 Project Overview
A modern Spring Boot application designed to manage business workflows: Customers, Products, and Invoices. It features a rich, responsive web interface built with Vaadin.

## 🛠 Tech Stack
- **Core**: Java 25, Spring Boot 4.0.0
- **UI Framework**: Vaadin 25.1.0 (Server-side, Java-based components)
- **Data Access**: Spring Data JDBC
- **Database**: PostgreSQL
- **Utilities**: Lombok (Annotation Processor)
- **Infrastructure**: Docker Compose (Local development), Testcontainers (Integration Testing)

## 📁 Project Structure
- `com.rkscientificindustries.invoice`
    - `backend/`: Business logic, services, and repositories.
        - `customer/`: Customer management (entities, repositories).
        - `product/`: Product catalog management.
        - `invoice/`: Invoice generation and line items.
        - `config/`: Spring configuration and `@ConfigurationProperties` bindings (`InvoiceProperties`, `DataConfig`).
        - `data/`: Demo-profile data loaders and mock constants (`CustomerDataLoader`, `ProductDataLoader`, `InvoiceDataLoader`).
        - `utils/`: Common helpers.
    - `ui/`: Vaadin views and layouts.
        - `customers/`, `products/`, `invoices/`: View components and CRUD interfaces.
        - `utils/`: Shared UI components (for example, `FabButton`).
        - `MainLayout.java`: Navigation and application shell.
    - `InvoiceApplication.java`: Main entry point.

## 💾 Data Model
The application uses a relational schema defined in `src/main/resources/db/migration/V1__init_schema.sql` (managed by Flyway):
- **Customers**: Business entities with GSTIN, address, and contact info.
- **Products**: Items with HSN codes, unit prices, and GST rates.
- **Invoices**: Header records with billing/shipping info and totals.
- **Line Items**: Detailed breakdowns of products on each invoice.

## ⚙️ Development Workflows
### Running the App
- **Local Development**: `./gradlew bootRun`
- **With Mock Data**: `./gradlew bootRun --args='--spring.profiles.active=demo'`
    - This generates 10 customers, 10 products, and 5 invoices by default.
    - Customize via `--invoice.customers=X --invoice.products=Y --invoice.invoices=Z`

### Useful Commands

| Gradle Command             | Description                                   |
|:---------------------------|:----------------------------------------------|
| `./gradlew bootRun`        | Run the application.                          |
| `./gradlew build`          | Build the application.                        |
| `./gradlew test`           | Run tests.                                    |
| `./gradlew bootJar`        | Package the application as a JAR.             |
| `./gradlew bootBuildImage` | Package the application as a container image. |

### Database Management
- The project uses Flyway migrations (`db/migration/*.sql`) for database initialization and evolution.
- Local PostgreSQL is automatically managed via `compose.yml` if Docker is running.

## 🧪 Testing Strategy
The project leverages Spring Boot 4's testing capabilities and **JUnit 6**. Agents MUST follow the modern patterns and best practices defined in the [Spring Boot Testing Skill](.agent/skills/spring-boot-testing/SKILL.md).

- **Unit Tests**: Focus on business logic in `backend/` using mock environments.
- **Slicing**: Use Spring Boot's test slices for performance (e.g., `@DataJdbcTest`, `@JsonTest`).
- **Integration Tests**: Use `@SpringBootTest` combined with Testcontainers. Use `@ServiceConnection` to automatically link containers like PostgreSQL.
- **Vaadin Testing**: Verification of UI logic should be done via unit tests where possible, or integration tests for view-to-backend wiring.

## 🧠 Coding Guidelines for Agents
1. **Model Management**: Use Lombok annotations (`@Data`, `@Builder`, etc.) to keep entities clean.
2. **Data Access**: Standard Repository pattern via Spring Data JDBC. Note: Avoid JPA-specific assumptions.
3. **UI Development**: 
    - Prefer Vaadin's typed components.
    - Use `VerticalLayout` and `HorizontalLayout` for alignment.
    - Ensure new views are added to `MainLayout` or tagged with `@Route`.
4. **Validation**: Use `@Valid` and JSR-303 annotations for bean validation.
5. **Container Integration**: Leverage `TestcontainersConfiguration.java` for consistent environment setup across tests.

## 📝 Commit Messages & Pull Requests

### **Commit Messages**
Follow the **Conventional Commits (v1.0.0)** specification as detailed in the [Conventional Commit Skill](.agents/skills/conventional-commit/SKILL.md).

**Core Rules:**
- Use imperative, present tense ("Add feature" not "Added feature").
- Types include: `feat`, `fix`, `build`, `chore`, `ci`, `docs`, `style`, `refactor`, `perf`, `test`.
- Breaking changes indicated by `!` or `BREAKING CHANGE:` footer.

### **Pull Requests**
Every pull request must clearly answer:
1. **What changed?**
2. **Why?**
3. **Breaking changes?**
4. **Server PR?** (If the change requires a coordinated server update)

**Note:** Comments should be complete sentences and end with a period.

## 🔑 Key Entry Points
- **Main View**: `com.rkscientificindustries.invoice.ui.HomeView`
- **Application Boot**: `com.rkscientificindustries.invoice.InvoiceApplication`
- **Database Schema**: `src/main/resources/db/migration/V1__init_schema.sql`
