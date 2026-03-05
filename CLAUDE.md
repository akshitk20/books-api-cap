# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a SAP Cloud Application Programming (CAP) model Java application built with Spring Boot. It implements a bookshop API with CDS (Core Data Services) schema definitions and Java service handlers.

The project combines:
- **CDS models** (`.cds` files) for defining domain models and services
- **Java/Spring Boot** backend for service implementation
- **Maven** for build and dependency management
- **H2 database** for local development (in-memory)

## Architecture

### Core Structure

- **`db/`** - Domain model definitions (CDS schema)
  - `schema.cds` - Main bookshop domain model (Books, Publishers, Reviews)
  - `employeemanagement.cds` - Employee management domain model (separate context)
  - `data/` - CSV files for seeding initial data

- **`srv/`** - Service layer
  - `catalog-service.cds` - Service definition exposing Books, Publishers, Reviews as OData entities
  - `src/main/java/customer/books_api_cap/` - Java implementation
    - `Application.java` - Spring Boot entry point
    - `handlers/CatalogServiceHandler.java` - Event handlers for CatalogService
  - `src/gen/java/` - Auto-generated POJOs from CDS models (DO NOT edit manually)

- **`app/`** - UI/frontend content (currently empty)

### CDS Model Architecture

The CDS compiler generates Java POJOs from `.cds` files during the Maven build. These are located in `srv/src/gen/java/cds/gen/` and include:
- Entity classes for domain models (Books, Publishers, Reviews, etc.)
- Service interfaces (CatalogService)
- Metadata classes (Books_, Publishers_, etc.) for type-safe CQN queries

**Important**: Never manually edit files in `srv/src/gen/java/` as they are regenerated on build.

### Service Handlers

Service handlers in `srv/src/main/java/customer/books_api_cap/handlers/` implement custom business logic:
- Use `@ServiceName` to bind to CDS service definitions
- Use `@On`, `@Before`, `@After` annotations to hook into CDS events
- Access database via injected `PersistenceService`
- Use CQN (CDS Query Notation) for type-safe queries with generated metadata classes

## Development Commands

### Build and Run

```bash
# Full Maven build (from root)
mvn clean install

# Run the application locally
mvn spring-boot:run -pl srv

# Or from srv directory
cd srv && mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

### CDS Development

```bash
# Watch mode - auto-reload on file changes (development)
cds watch

# Build CDS model for Java
cds build --for java

# Deploy to H2 (local development)
cds deploy --to h2
```

### Code Generation

When you modify `.cds` files, regenerate Java POJOs:

```bash
# From root directory
mvn clean compile -pl srv
```

This runs the `cds-maven-plugin` which:
1. Resolves CDS models
2. Builds the model (`cds build --for java`)
3. Generates Java POJOs into `srv/src/gen/java/`
4. Creates H2 schema SQL

## Key Technologies

- **Java 21** - Language version
- **Spring Boot 3.5.6** - Application framework
- **SAP CDS 4.4.2** - Core Data Services framework
- **Maven 3.6.3+** - Build tool (enforced by maven-enforcer-plugin)
- **H2** - In-memory database for development

## OData Endpoints

The CatalogService exposes OData V4 endpoints at `/CatalogService`:

- `GET /CatalogService/Books` - List all books
- `GET /CatalogService/Publishers` - List all publishers
- `GET /CatalogService/Reviews` - List all reviews
- `GET /CatalogService/getHighStockBooks()` - Custom function

All entities are marked `@readonly` in the service definition.

## Adding New Features

### Adding a New Entity

1. Define entity in `db/schema.cds` or create new `.cds` file in `db/`
2. Add CSV data file in `db/data/` (format: `namespace-EntityName.csv`)
3. Expose entity in service definition (e.g., `srv/catalog-service.cds`)
4. Run `mvn clean compile -pl srv` to regenerate POJOs
5. Add custom handler in `srv/src/main/java/customer/books_api_cap/handlers/` if needed

### Adding Custom Service Logic

1. Create/modify handler class in `handlers/` package
2. Annotate with `@Component` and `@ServiceName(YourService_.CDS_NAME)`
3. Implement `EventHandler` interface
4. Use `@On`, `@Before`, or `@After` annotations on methods
5. Method parameter should be `EventContext` or specific context type

Example:
```java
@On(event = "yourCustomFunction")
public void handleCustomFunction(EventContext context) {
    // Implementation
}
```

## Common Patterns

### Type-Safe CQN Queries

Use generated metadata classes (with trailing underscore) for type-safe queries:

```java
CqnSelect query = Select.from(Books_.class)
    .where(b -> b.stock().gt(100));
List<Books> results = persistenceService.run(query).listOf(Books.class);
```

### CDS Aspects

The schema uses standard CDS aspects:
- `cuid` - Auto-generated UUID key (`ID : UUID`)
- `managed` - Audit fields (createdAt, createdBy, modifiedAt, modifiedBy)
- `Archivable` - Custom aspect (isArchived, archivedAt)

## Notes

- The project uses CI-friendly Maven versions with `${revision}` property
- Generated POJOs use enhanced naming (`betterNames: true`) and linked interfaces
- OData adapter is included for OData V4 protocol support
- Spring Boot DevTools is enabled for hot-reload during development
