# Quick Start Guide: Caching & Authentication

## Running the Application

### Local Development (No Authentication)
```bash
# Set security disabled
export SECURITY_ENABLED=false

# Run from project root
mvn spring-boot:run

# Application starts on http://localhost:9081
```

### MCP Mode (Local Development)
```bash
# Run with MCP flag (security automatically disabled)
java -jar srv/target/books-api-cap-exec.jar --mcp-mode
```

### Production Mode (With XSUAA Authentication)
```bash
# Security enabled by default
mvn spring-boot:run

# Or set explicitly
export SECURITY_ENABLED=true
mvn spring-boot:run
```

## Testing Cache Performance

### First Request (Cache Miss)
```bash
# Measure time for first request
time curl http://localhost:9081/CatalogService/Books
# Expected: ~60-110ms
```

### Second Request (Cache Hit)
```bash
# Measure time for cached request
time curl http://localhost:9081/CatalogService/Books
# Expected: ~5-15ms (60-110x faster!)
```

## Cache Configuration

### View Cache Statistics
The cache is configured with `recordStats()` enabled. To view metrics, add Spring Boot Actuator:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Then access: `http://localhost:9081/actuator/caches`

### Adjust TTL Values
Edit `srv/src/main/resources/application.yaml`:

```yaml
cache:
  ttl:
    books: 600        # 10 minutes
    authors: 600      # 10 minutes
    publishers: 600   # 10 minutes
    reviews: 300      # 5 minutes
    resources: 1200   # 20 minutes
```

### Clear Cache (Manual)
Since this is in-memory cache, restart the application:
```bash
# Kill current process
pkill -f books-api-cap

# Restart
mvn spring-boot:run
```

## Security Configuration

### Disable Security for Testing
**Option 1: Environment Variable**
```bash
SECURITY_ENABLED=false mvn spring-boot:run
```

**Option 2: application.yaml**
```yaml
spring:
  security:
    enabled: false
```

**Option 3: MCP Mode**
```bash
java -jar srv/target/books-api-cap-exec.jar --mcp-mode
```

### Enable Security (Production)
Security is enabled by default. To use with XSUAA:

1. Create `xs-security.json` in project root
2. Bind XSUAA service in BTP
3. Deploy to Cloud Foundry

## Available Endpoints

### Public (Always Accessible)
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info

### Protected (Requires JWT Token)
- `GET /CatalogService/Books` - List all books
- `GET /CatalogService/Books(id)` - Get single book
- `GET /CatalogService/Authors` - List all authors
- `GET /CatalogService/Publishers` - List all publishers
- `GET /CatalogService/Reviews` - List all reviews
- `GET /CatalogService/getHighStockBooks()` - Books with high stock

## Troubleshooting

### Cache Not Working
**Symptom:** Every request takes same time

**Solution:**
1. Check logs for cache hit/miss (enable debug logging):
   ```yaml
   logging:
     level:
       org.springframework.cache: DEBUG
   ```
2. Verify `@EnableCaching` is active in `CacheConfig.java`
3. Ensure calls go through `BookshopCatalogService` (not direct `PersistenceService`)

### Security Blocking Requests
**Symptom:** 401 Unauthorized errors

**Solution:**
1. Check `spring.security.enabled` property:
   ```bash
   echo $SECURITY_ENABLED
   ```
2. Set to false for local testing:
   ```bash
   export SECURITY_ENABLED=false
   ```
3. Or use MCP mode which automatically disables security

### Build Failures
**Symptom:** Maven build fails

**Solution:**
1. Clean build:
   ```bash
   mvn clean install
   ```
2. Check Java version (requires Java 21):
   ```bash
   java -version
   ```
3. Check Maven version (requires Maven 3.6.3+):
   ```bash
   mvn -version
   ```

### Tests Failing
**Symptom:** Unit tests fail

**Solution:**
1. Check if mock injection is correct:
   - Tests should mock `BookshopCatalogService`, not `PersistenceService`
2. Run tests with verbose output:
   ```bash
   mvn test -X
   ```

## Performance Monitoring

### Expected Performance
| Scenario | Response Time | Cache Status |
|----------|---------------|--------------|
| First request | 60-110ms | MISS |
| Cached request | 5-15ms | HIT |
| After TTL expires | 60-110ms | MISS (refresh) |

### Measuring Cache Hit Ratio
Enable statistics in logs:
```java
// In CacheConfig.java (already enabled)
.recordStats()
```

View stats programmatically:
```java
@Autowired
private CacheManager cacheManager;

public void printStats() {
    Cache cache = cacheManager.getCache("books:all");
    CaffeineCache caffeineCache = (CaffeineCache) cache;
    CacheStats stats = caffeineCache.getNativeCache().stats();
    System.out.println("Hit rate: " + stats.hitRate());
}
```

## Advanced Configuration

### Increase Cache Size
Edit `CacheConfig.java`:
```java
.maximumSize(2000)  // Increase from 1000 to 2000 entries
```

### Change Eviction Strategy
```java
// Time-based (current)
.expireAfterWrite(defaultTtl, TimeUnit.SECONDS)

// Access-based (alternative)
.expireAfterAccess(defaultTtl, TimeUnit.SECONDS)

// Combined
.expireAfterWrite(600, TimeUnit.SECONDS)
.expireAfterAccess(300, TimeUnit.SECONDS)
```

### Add Cache Warming (Pre-population)
Create a startup listener:
```java
@Component
public class CacheWarmer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private BookshopCatalogService catalogService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Pre-populate cache on startup
        catalogService.getAllBooks();
        catalogService.getAllAuthors();
        catalogService.getAllPublishers();
    }
}
```

## Integration with Claude Code MCP

### Configure MCP Server
Already configured in `.claude/settings.local.json`:

```json
{
  "mcpServers": {
    "bookshop": {
      "command": "java",
      "args": [
        "-jar",
        "srv/target/books-api-cap-exec.jar",
        "--mcp-mode"
      ]
    }
  }
}
```

### Test MCP Tools
All 8 tools now benefit from caching:
1. `list_books` - Cached (10 min)
2. `search_books` - Cached by search term (10 min)
3. `get_book` - Cached by book ID (10 min)
4. `list_authors` - Cached (10 min)
5. `get_author` - Cached by author ID (10 min)
6. `list_publishers` - Cached (10 min)
7. `get_high_stock_books` - Cached by minStock threshold (10 min)
8. `get_book_reviews` - Cached by book ID (5 min)

## Need Help?

- Check logs: `tail -f srv/target/spring.log`
- Enable debug mode: `mvn spring-boot:run -Dspring-boot.run.arguments=--debug`
- View full documentation: See `IMPLEMENTATION-SUMMARY.md`
- Architecture details: See `CLAUDE.md`
