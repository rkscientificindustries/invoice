# invoice
Invoice Management System

### Database Setup
```bash
docker container run -d --rm --name invoice-postgres \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=secret \
  -e POSTGRES_DB=invoice_db \
  -p 5433:5432 \
  postgres:18
```

### Demo Profile and Mock Data Generation

The application supports a `demo` profile that automatically generates mock customer data on startup.

#### Running with Demo Profile

**Default (10 customers):**
```bash
./gradlew bootRun --args='--spring.profiles.active=demo'
```

**Custom number of customers:**
```bash
./gradlew bootRun --args='--spring.profiles.active=demo --invoice.customers=50'
```

**Using environment variables:**
```bash
SPRING_PROFILES_ACTIVE=demo INVOICE_CUSTOMERS=25 ./gradlew bootRun
```
