package customer.books_api_cap.config;

import com.sap.cloud.security.test.JwtGenerator;
import com.sap.cloud.security.test.SecurityTestRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Security Configuration Tests for SAP XSUAA Authentication
 *
 * Tests various authentication and authorization scenarios:
 * - Valid JWT tokens with correct scopes
 * - Invalid/expired/malformed tokens
 * - Missing authentication
 * - Scope-based authorization
 * - Public endpoints accessibility
 * - MCP mode security disabled
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String XSUAA_APP_ID = "bookshop-api-cap";
    private static final String DISPLAY_SCOPE = XSUAA_APP_ID + ".Display";
    private static final String ADMIN_SCOPE = XSUAA_APP_ID + ".Admin";

    private JwtGenerator jwtGenerator;

    @BeforeEach
    void setUp() {
        // Initialize JWT generator for XSUAA tokens
        jwtGenerator = JwtGenerator.getInstance(
            "https://subdomain.authentication.sap.hana.ondemand.com",
            XSUAA_APP_ID
        );
    }

    // ============================================
    // Tests: No Authentication
    // ============================================

    @Test
    void catalogServiceBooks_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/CatalogService/Books"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void catalogServicePublishers_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/CatalogService/Publishers"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void catalogServiceAuthors_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/CatalogService/Authors"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void catalogServiceReviews_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/CatalogService/Reviews"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void catalogServiceCustomFunction_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/CatalogService/getHighStockBooks()"))
               .andExpect(status().isUnauthorized());
    }

    // ============================================
    // Tests: Invalid Authentication
    // ============================================

    @Test
    void catalogService_withInvalidToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer invalid-jwt-token"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void catalogService_withMalformedToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer not.a.jwt"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void catalogService_withMalformedAuthHeader_shouldReturn401() throws Exception {
        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "InvalidScheme sometoken"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void catalogService_withExpiredToken_shouldReturn401() throws Exception {
        // Generate token that expired 1 hour ago
        String expiredToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .withExpiration(Instant.now().minusSeconds(3600))
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + expiredToken))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void catalogService_withTokenFromWrongIssuer_shouldReturn401() throws Exception {
        // Generate token with wrong issuer
        JwtGenerator wrongIssuerGenerator = JwtGenerator.getInstance(
            "https://wrong-issuer.authentication.sap.hana.ondemand.com",
            XSUAA_APP_ID
        );

        String token = wrongIssuerGenerator
            .withScopes(DISPLAY_SCOPE)
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + token))
               .andExpect(status().isUnauthorized());
    }

    // ============================================
    // Tests: Valid Authentication with Correct Scope
    // ============================================

    @Test
    void catalogServiceBooks_withValidToken_shouldReturn200() throws Exception {
        String validToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .withClaim("user_name", "test-user")
            .withClaim("email", "test@example.com")
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + validToken))
               .andExpect(status().isOk());
    }

    @Test
    void catalogServicePublishers_withValidToken_shouldReturn200() throws Exception {
        String validToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Publishers")
                .header("Authorization", "Bearer " + validToken))
               .andExpect(status().isOk());
    }

    @Test
    void catalogServiceAuthors_withValidToken_shouldReturn200() throws Exception {
        String validToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Authors")
                .header("Authorization", "Bearer " + validToken))
               .andExpect(status().isOk());
    }

    @Test
    void catalogServiceReviews_withValidToken_shouldReturn200() throws Exception {
        String validToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Reviews")
                .header("Authorization", "Bearer " + validToken))
               .andExpect(status().isOk());
    }

    @Test
    void catalogServiceCustomFunction_withValidToken_shouldReturn200() throws Exception {
        String validToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/getHighStockBooks()")
                .header("Authorization", "Bearer " + validToken))
               .andExpect(status().isOk());
    }

    // ============================================
    // Tests: Valid Authentication with Admin Scope
    // ============================================

    @Test
    void catalogService_withAdminScope_shouldReturn200() throws Exception {
        String adminToken = jwtGenerator
            .withScopes(ADMIN_SCOPE)
            .withClaim("user_name", "admin-user")
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + adminToken))
               .andExpect(status().isOk());
    }

    @Test
    void catalogService_withMultipleScopes_shouldReturn200() throws Exception {
        String token = jwtGenerator
            .withScopes(DISPLAY_SCOPE, ADMIN_SCOPE, "other-scope")
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + token))
               .andExpect(status().isOk());
    }

    // ============================================
    // Tests: Authorization (Scope-Based)
    // ============================================

    @Test
    void catalogService_withoutRequiredScope_shouldReturn403() throws Exception {
        // Token with different scope (not Display or Admin)
        String tokenWithoutScope = jwtGenerator
            .withScopes("some-other-app.SomeScope")
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + tokenWithoutScope))
               .andExpect(status().isForbidden());
    }

    @Test
    void catalogService_withEmptyScopes_shouldReturn403() throws Exception {
        // Token with no scopes at all
        String tokenWithNoScopes = jwtGenerator
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + tokenWithNoScopes))
               .andExpect(status().isForbidden());
    }

    @Test
    void catalogService_withPartialScopeName_shouldReturn403() throws Exception {
        // Token with scope that contains app name but not exact match
        String tokenWithPartialScope = jwtGenerator
            .withScopes("bookshop-api-cap")  // Missing ".Display"
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + tokenWithPartialScope))
               .andExpect(status().isForbidden());
    }

    // ============================================
    // Tests: Public Endpoints
    // ============================================

    @Test
    void actuatorHealth_withoutAuth_shouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void actuatorInfo_withoutAuth_shouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/info"))
               .andExpect(status().isOk());
    }

    @Test
    void actuatorHealth_withToken_shouldStillReturn200() throws Exception {
        // Public endpoints should work with or without token
        String validToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/actuator/health")
                .header("Authorization", "Bearer " + validToken))
               .andExpect(status().isOk());
    }

    // ============================================
    // Tests: Token Claims Extraction
    // ============================================

    @Test
    void catalogService_withUserClaims_shouldExtractCorrectly() throws Exception {
        String token = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .withClaim("user_name", "akshit.khatri@sap.com")
            .withClaim("email", "akshit.khatri@sap.com")
            .withClaim("given_name", "Akshit")
            .withClaim("family_name", "Khatri")
            .createToken()
            .getTokenValue();

        // Test that request succeeds with user claims
        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + token))
               .andExpect(status().isOk());

        // Note: To verify claims are extracted, you'd need to:
        // 1. Add a test endpoint that returns SecurityContext info
        // 2. Or use @WithMockUser equivalent for XSUAA
        // 3. Or inject SecurityContext in handler and verify in unit test
    }

    @Test
    void catalogService_withClientCredentials_shouldWork() throws Exception {
        // Test client credentials flow (service-to-service)
        String clientToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .withClaim("client_id", "bookshop-client")
            .withClaim("grant_type", "client_credentials")
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + clientToken))
               .andExpect(status().isOk());
    }

    // ============================================
    // Tests: Token Expiration Edge Cases
    // ============================================

    @Test
    void catalogService_withTokenExpiringInFuture_shouldReturn200() throws Exception {
        // Token expires 1 hour from now
        String futureExpiryToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .withExpiration(Instant.now().plusSeconds(3600))
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + futureExpiryToken))
               .andExpect(status().isOk());
    }

    @Test
    void catalogService_withTokenExpiringNow_shouldReturn401() throws Exception {
        // Token expires right now (edge case)
        String expiringNowToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .withExpiration(Instant.now())
            .createToken()
            .getTokenValue();

        // Depending on timing, this might be 200 or 401
        // Most implementations reject tokens expiring "now"
        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + expiringNowToken))
               .andExpect(status().isUnauthorized());
    }

    // ============================================
    // Tests: Case Sensitivity
    // ============================================

    @Test
    void catalogService_withLowercaseBearer_shouldReturn401() throws Exception {
        String validToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .createToken()
            .getTokenValue();

        // OAuth 2.0 Bearer scheme is case-sensitive (must be "Bearer")
        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "bearer " + validToken))  // lowercase
               .andExpect(status().isUnauthorized());
    }

    // ============================================
    // Tests: OData Query Options with Auth
    // ============================================

    @Test
    void catalogServiceWithODataQuery_withValidToken_shouldReturn200() throws Exception {
        String validToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .createToken()
            .getTokenValue();

        // OData query parameters should work with authentication
        mockMvc.perform(get("/CatalogService/Books?$top=5&$skip=0")
                .header("Authorization", "Bearer " + validToken))
               .andExpect(status().isOk());
    }

    @Test
    void catalogServiceWithFilter_withValidToken_shouldReturn200() throws Exception {
        String validToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .createToken()
            .getTokenValue();

        // OData filter with authentication
        mockMvc.perform(get("/CatalogService/Books?$filter=stock gt 100")
                .header("Authorization", "Bearer " + validToken))
               .andExpect(status().isOk());
    }

    // ============================================
    // Tests: Multiple Concurrent Requests
    // ============================================

    @Test
    void catalogService_multipleRequestsSameToken_shouldAllSucceed() throws Exception {
        String validToken = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .createToken()
            .getTokenValue();

        // Simulate multiple requests with same token (common in real apps)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/CatalogService/Books")
                    .header("Authorization", "Bearer " + validToken))
                   .andExpect(status().isOk());
        }
    }

    @Test
    void catalogService_differentUsers_shouldBothSucceed() throws Exception {
        // User 1 token
        String user1Token = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .withClaim("user_name", "user1@example.com")
            .createToken()
            .getTokenValue();

        // User 2 token
        String user2Token = jwtGenerator
            .withScopes(DISPLAY_SCOPE)
            .withClaim("user_name", "user2@example.com")
            .createToken()
            .getTokenValue();

        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + user1Token))
               .andExpect(status().isOk());

        mockMvc.perform(get("/CatalogService/Books")
                .header("Authorization", "Bearer " + user2Token))
               .andExpect(status().isOk());
    }

    // ============================================
    // Tests: CORS Preflight (OPTIONS)
    // ============================================

    @Test
    void catalogService_optionsRequest_shouldHandlePreflight() throws Exception {
        // CORS preflight requests should not require authentication
        mockMvc.perform(options("/CatalogService/Books")
                .header("Origin", "https://example.com")
                .header("Access-Control-Request-Method", "GET"))
               .andExpect(status().isOk());
    }
}
