package org.simplecache.cache;

public class CacheEntry {

    private final String key;
    private final String value;
    private final String ttl;

    public CacheEntry(String key, String value) {
        this(key, value, null);
    }

    public CacheEntry(String key, String value, String ttl) {
        this.key = key;
        this.value = value;
        this.ttl = ttl;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getTtl() {
        return ttl;
    }
}
