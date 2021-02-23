package org.simplecache.cache;

import java.time.Duration;

public final class CacheValue {

    private final String value;
    private final Duration ttl;

    public CacheValue(String value, Duration ttl) {
        this.value = value;
        this.ttl = ttl;
    }

    public String getValue() {
        return value;
    }

    public Duration getTtl() {
        return ttl;
    }
}
