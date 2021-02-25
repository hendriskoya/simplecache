package org.simplecache.cache;

public class CacheEntry {

    private final String key;
    private final String value;
    private final long exp;

    public CacheEntry(String key, String value) {
        this(key, value, -1);
    }

    public CacheEntry(String key, String value, long exp) {
        this.key = key;
        this.value = value;
        this.exp = exp;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public long getExp() {
        return exp;
    }
}
