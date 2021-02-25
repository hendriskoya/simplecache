package org.simplecache.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import org.ehcache.expiry.ExpiryPolicy;

public class SimpleCacheExpiryPolicy implements ExpiryPolicy<String, CacheValue> {

    @Override
    public Duration getExpiryForCreation(String key, CacheValue value) {
        System.out.println("**************");
        System.out.println("** getExpiryForCreation - key: " + key + " - value: " + value.getValue() + " - exp: " + value.getExp() + " - createdAt: " + Instant.ofEpochMilli(value.getCreatedAt()));
        System.out.println("**************");
        return getDuration(value);
    }

    @Override
    public Duration getExpiryForAccess(String key, Supplier<? extends CacheValue> value) {
        System.out.println("**************");
        System.out.println("** getExpiryForAccess - key: " + key + " - value: " + value.get().getValue() + " - exp: " + value.get().getValue() + " - createdAt: " + Instant.ofEpochMilli(value.get().getCreatedAt()));
        System.out.println("**************");
        return null;
    }

    @Override
    public Duration getExpiryForUpdate(String key, Supplier<? extends CacheValue> oldValue, CacheValue newValue) {
        System.out.println("**************");
        System.out.println("** getExpiryForUpdate - key: " + key + " - oldValue: " + oldValue.get().getValue() + " - oldExp: " + oldValue.get().getExp() + " - createdAt: " + Instant.ofEpochMilli(oldValue.get().getCreatedAt())
                + " - newValue: " + newValue.getValue() + " - newExp: " + newValue.getExp() + " - createdAt: " + Instant.ofEpochMilli(newValue.getCreatedAt()));
        System.out.println("**************");
        return getDuration(newValue);
    }

    private Duration getDuration(CacheValue value) {
        if (value.getExp() == -1) {
            System.out.println("ttl in seconds: " + INFINITE);
            return ExpiryPolicy.INFINITE;
        } else {
            long now = Instant.now().toEpochMilli() / 1000;
            Duration ttl = Duration.ofSeconds(value.getExp() - now);
            System.out.println("ttl in seconds: " + ttl.toSeconds());
            return ttl;
        }
    }
}