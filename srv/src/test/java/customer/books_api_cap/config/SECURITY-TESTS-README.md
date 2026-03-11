# Security Test Suite for XSUAA Authentication

This directory contains comprehensive security tests for the SAP XSUAA authentication implementation.

## Test Coverage

### SecurityConfigTest.java

**Total Test Cases: 40+**

#### 1. No Authentication Tests (5 tests)
- All catalog service endpoints return 401 without token
- Custom function endpoint returns 401 without token

#### 2. Invalid Authentication Tests (6 tests)
- Invalid JWT token format
- Malformed tokens
- Malformed authorization headers
- Expired tokens
- Tokens from wrong issuer
- Token expiration edge cases

#### 3. Valid Authentication Tests (6 tests)
- Valid token with Display scope
- Access to all catalog endpoints (Books, Publishers, Authors, Reviews)
- Custom function with authentication
- Admin scope access

#### 4. Authorization (Scope-Based) Tests (3 tests)
- Missing required scope returns 403
- Empty scopes returns 403
- Partial scope name doesn't match

#### 5. Public Endpoints Tests (3 tests)
- Actuator health/info accessible without auth
- Public endpoints work with or without token

#### 6. Token Claims Tests (2 tests)
- User claims extraction
- Client credentials flow

#### 7. Token Expiration Tests (2 tests)
- Future expiry works
- Immediate expiry rejected

#### 8. Edge Cases (8 tests)
- Case sensitivity of "Bearer" scheme
- OData query parameters with auth
- Multiple concurrent requests
- Different users
- CORS preflight handling

---

## Running the Tests

### Run All Security Tests

```bash
# From project root
mvn test -pl srv -Dtest=SecurityConfigTest

# Or from srv directory
cd srv
mvn test -Dtest=SecurityConfigTest
```

### Run Specific Test Category

```bash
# Run only authentication tests
mvn test -Dtest=SecurityConfigTest#catalogService*

# Run only authorization tests
mvn test -Dtest=SecurityConfigTest#*Scope*

# Run only public endpoint tests
mvn test -Dtest=SecurityConfigTest#actuator*
```

### Run All Tests Including Security

```bash
# Run entire test suite
mvn test -pl srv
```

---

## Test Configuration

### Test Profile (application-test.yaml)

The tests use a dedicated test profile with:
- **Security disabled by default** (individual tests enable as needed)
- **Lightweight caching** (60s TTL, max 100 entries)
- **Random server port** (avoids conflicts)
- **DEBUG logging** for security and cache

### Mock JWT Tokens

Tests use SAP's `java-security-test` library to generate mock XSUAA JWT tokens:

```java
String token = JwtGenerator.getInstance(
    "https://subdomain.authentication.sap.hana.ondemand.com",
    "bookshop-api-cap"
)
.withScopes("bookshop-api-cap.Display")
.withClaim("user_name", "test-user")
.createToken()
.getTokenValue();
```

---

## Understanding Test Results

### ✅ Passing Tests Mean:

1. **401 Tests Pass**: Endpoints properly reject unauthenticated requests
2. **403 Tests Pass**: Scope-based authorization working correctly
3. **200 Tests Pass**: Valid tokens with correct scopes are accepted
4. **Public Endpoint Tests Pass**: Health checks accessible without auth

### ❌ Failing Tests Might Indicate:

1. **SecurityConfig not loaded**: Check `@ConditionalOnProperty` conditions
2. **XSUAA dependency missing**: Verify `pom.xml` has `xsuaa-spring-boot-starter`
3. **JWT validation failure**: Check XSUAA issuer URL configuration
4. **Scope mismatch**: Verify scope names in `xs-security.json` match test expectations

---

## Test Dependencies

### Required in pom.xml

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Spring Security Test -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- SAP XSUAA -->
<dependency>
    <groupId>com.sap.cloud.security.xsuaa</groupId>
    <artifactId>xsuaa-spring-boot-starter</artifactId>
    <version>3.5.3</version>
</dependency>

<!-- SAP Cloud Security Test (for mock JWT) -->
<dependency>
    <groupId>com.sap.cloud.security</groupId>
    <artifactId>java-security-test</artifactId>
    <version>3.5.3</version>
    <scope>test</scope>
</dependency>
```

---

## Customizing Tests

### Change XSUAA App ID

```java
private static final String XSUAA_APP_ID = "your-app-id";
```

### Add New Scope Tests

```java
@Test
void catalogService_withCustomScope_shouldReturn200() throws Exception {
    String token = jwtGenerator
        .withScopes("bookshop-api-cap.CustomScope")
        .createToken()
        .getTokenValue();

    mockMvc.perform(get("/CatalogService/Books")
            .header("Authorization", "Bearer " + token))
           .andExpect(status().isOk());
}
```

### Test Method-Level Authorization

If you add `@PreAuthorize` annotations:

```java
// In your service handler
@PreAuthorize("hasAuthority('SCOPE_bookshop-api-cap.Admin')")
public void adminOperation() { ... }

// In test
@Test
void adminOperation_withoutAdminScope_shouldReturn403() throws Exception {
    String displayOnlyToken = jwtGenerator
        .withScopes("bookshop-api-cap.Display")  // No Admin
        .createToken()
        .getTokenValue();

    mockMvc.perform(post("/admin/operation")
            .header("Authorization", "Bearer " + displayOnlyToken))
           .andExpect(status().isForbidden());
}
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Security Tests

on: [push, pull_request]

jobs:
  security-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run Security Tests
        run: mvn test -pl srv -Dtest=SecurityConfigTest
```

### Jenkins Pipeline Example

```groovy
stage('Security Tests') {
    steps {
        sh 'mvn test -pl srv -Dtest=SecurityConfigTest'
    }
}
```

---

## Troubleshooting

### Tests Fail with "SecurityConfig not found"

**Solution**: Ensure `spring.security.enabled=false` in `application-test.yaml` OR your test explicitly enables security.

### Tests Fail with "401 expected but was 200"

**Cause**: Security is disabled globally.

**Solution**: Check `@ActiveProfiles("test")` is present and test profile has correct security config.

### Tests Fail with "Invalid JWT signature"

**Cause**: XSUAA configuration mismatch.

**Solution**: Verify `JwtGenerator` issuer URL matches your XSUAA service configuration.

### Tests Pass Locally but Fail in CI

**Cause**: Different Spring profiles or environment variables.

**Solution**: Ensure CI uses same profile as local tests (`test` profile).

---

## Future Enhancements

### Potential Additions

1. **Integration Tests with Real XSUAA Mock Server**
   - Run full OAuth flow
   - Test token refresh
   - Test multiple tenants

2. **Performance Tests**
   - JWT validation overhead
   - Concurrent user scenarios
   - Token caching effectiveness

3. **Security Audit Tests**
   - SQL injection attempts
   - XSS payloads
   - CSRF token validation
   - Rate limiting

4. **Authorization Tests**
   - Role-based access control
   - Data-level security (row-level filtering)
   - Tenant isolation

---

## References

- [SAP Cloud Security Java Library](https://github.com/SAP/cloud-security-xsuaa-integration)
- [Spring Security Testing](https://docs.spring.io/spring-security/reference/servlet/test/index.html)
- [XSUAA Authentication Documentation](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/51ec15a8979e497fbcaadf80da9b63ba.html)

---

## Test Maintenance

**When to Update Tests:**

1. **New endpoints added**: Add corresponding authentication tests
2. **Scope requirements change**: Update scope names in tests
3. **XSUAA configuration changes**: Update `XSUAA_APP_ID` and scopes
4. **New authorization rules**: Add authorization tests
5. **Spring Security upgrade**: Verify test compatibility

**Best Practices:**

- Keep scope names consistent with `xs-security.json`
- Use descriptive test names following `method_condition_expectedResult` pattern
- Group related tests with comments
- Update this README when adding new test categories
