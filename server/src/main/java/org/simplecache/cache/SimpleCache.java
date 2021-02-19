package org.simplecache.cache;

import java.util.Iterator;
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
    private final Cache<String, String> cache;

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
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                        ResourcePoolsBuilder.heap(100)).build());

//        cache.put(1L, "da one!");
//        String value = cache.get(1L);

    }

    public void close() {
        cacheManager.close();
    }

    public void set(String key, String value) {
        cache.put(key, value);
    }

    public String get(String key) {
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

    public Iterator<Cache.Entry<String, String>> data() {
        return cache.iterator();
    }
}
