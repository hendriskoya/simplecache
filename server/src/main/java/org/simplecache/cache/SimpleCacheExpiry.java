package org.simplecache.cache;

import java.time.Duration;
import java.util.function.Supplier;
import org.ehcache.expiry.ExpiryPolicy;

public class SimpleCacheExpiry implements ExpiryPolicy<String, CacheValue> {

    @Override
    public Duration getExpiryForCreation(String key, CacheValue value) {
        return value.getTtl() != null ? value.getTtl() : ExpiryPolicy.INFINITE;
    }

    @Override
    public Duration getExpiryForAccess(String key, Supplier<? extends CacheValue> value) {
        return null;
    }

    @Override
    public Duration getExpiryForUpdate(String key, Supplier<? extends CacheValue> oldValue, CacheValue newValue) {
        return null;
    }
}