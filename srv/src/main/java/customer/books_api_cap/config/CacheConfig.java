package customer.books_api_cap.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine Cache Configuration
 *
 * Configures in-memory caching with TTL for read-only catalog operations.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.ttl.default:600}")
    private long defaultTtl;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "books:all", "books:search", "book:single",
            "authors:all", "author:single",
            "publishers:all",
            "books:highstock",
            "reviews:book",
            "resources:books", "resources:authors", "resources:publishers"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(defaultTtl, TimeUnit.SECONDS)
            .recordStats());  // Enable metrics for monitoring

        return cacheManager;
    }
}
