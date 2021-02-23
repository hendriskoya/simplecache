package org.simplecache.cache;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.simplecache.ConnectionManager;

/**
 * Esse CACHE precisa ser concorrente já que várias conexões farão uso
 */
public final class SimpleCache {

//    private Map<String, String> data;

    public static final SimpleCache INSTANCE = new SimpleCache();

    private final CacheManager cacheManager;
    private final Cache<String, CacheValue> cache;
    private final SimpleCacheExpiry simpleCacheExpiry = new SimpleCacheExpiry();

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
                        .withExpiry(simpleCacheExpiry)
//                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(20)))
                        .build());

//        cache.put(1L, "da one!");
//        String value = cache.get(1L);

    }

    public void close() {
        cacheManager.close();
    }

    public void set(String key, CacheValue value) {
        cache.put(key, value);
        //start - test
        hashCodeValidator.add(key);
        //end - test
    }

    public CacheValue get(String key) {
        return cache.get(key);
    }

    public boolean isSynchronized(ConnectionManager connectionManager) {
        /*System.out.println("connectionManager: " + connectionManager);
        if (connectionManager == null || connectionManager.nodes() == null || connectionManager.nodes().isEmpty()
                || cache.containsKey("name")) {
            return true;
        } else {
            //check if it was synchronized
            return false;
        }*/
        return false;
    }

    public Iterator<Cache.Entry<String, CacheValue>> data() {
        return cache.iterator();
    }

    public int cacheHashCode() {
        return hashCodeValidator.hashCode();
    }
}

