package org.simplecache.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

/**
 * Esse CACHE precisa ser concorrente já que várias conexões farão uso
 */
public final class SimpleCache {

//    private Map<String, String> data;

    public static final SimpleCache INSTANCE = new SimpleCache();

    private final CacheManager cacheManager;
    private final Cache<String, CacheValue> cache;
    private final SimpleCacheExpiryPolicy simpleCacheExpiryPolicy = new SimpleCacheExpiryPolicy();

    private final Set<String> hashCodeValidator = new HashSet<>();

    public SimpleCache() {
//        this.data = new HashMap<>();

        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                /*.withCache("preConfigured",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                ResourcePoolsBuilder.heap(100))
                                .build())*/
                .build(true);

//        Cache<Long, String> preConfigured = cacheManager.getCache("preConfigured", Long.class, String.class);

        cache = cacheManager.createCache("cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, CacheValue.class,
                        ResourcePoolsBuilder.heap(100))
                        .withExpiry(simpleCacheExpiryPolicy)
//                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(20)))
                        .build());

//        cache.put(1L, "da one!");
//        String value = cache.get(1L);

    }

    public void close() {
        cacheManager.close();
    }

    public CacheValue set(String key, String value, String ttl) {
        CacheValue cacheValue = SimpleCache.createCacheValue(value, ttl);

        return setCache(key, cacheValue);
    }

    public CacheValue set(String key, CacheValue cacheValue) {
        return setCache(key, cacheValue);
    }

    private CacheValue setCache(String key, CacheValue cacheValue) {
        CacheValue storedCacheValue = cache.get(key);
        long storedCreatedAt = -1;
        if (storedCacheValue != null) {
            storedCreatedAt = storedCacheValue.getCreatedAt();
        }
        if (cacheValue.getCreatedAt() > storedCreatedAt) {
            cache.put(key, cacheValue);
            //start - test
            hashCodeValidator.add(key);
            //end - test
            return cacheValue;
        }
        return storedCacheValue;
    }

    public CacheValue get(String key) {
        return cache.get(key);
    }

    public Iterator<Cache.Entry<String, CacheValue>> data() {
        return cache.iterator();
    }

    public int cacheHashCode() {
        return hashCodeValidator.hashCode();
    }

    public static CacheValue createCacheValue(String value, String ttl) {
        long exp = -1;
        if (ttl != null && !ttl.isBlank()) {
            long ttlInMillis = Duration.ofSeconds(Integer.valueOf(ttl)).toMillis();
            exp = Instant.now().plusMillis(ttlInMillis).toEpochMilli() / 1000;
        }
        long createdAt = Instant.now().toEpochMilli();
        return new CacheValue(value, exp, createdAt);
    }

    public static CacheValue createCacheValue(String value) {
        long createdAt = Instant.now().toEpochMilli();
        return new CacheValue(value, createdAt);
    }

    public static CacheValue createCacheValue(String value, long exp) {
        long createdAt = Instant.now().toEpochMilli();
        return new CacheValue(value, exp, createdAt);
    }
}

