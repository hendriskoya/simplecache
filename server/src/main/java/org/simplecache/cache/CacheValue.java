package org.simplecache.cache;

public final class CacheValue {

    private final String value;
    private final long exp;
    private final long createdAt;

    public CacheValue(String value, long exp, long createdAt) {
        this.value = value;
        this.exp = exp;
        this.createdAt = createdAt;
    }

    public CacheValue(String value, long createdAt) {
        this(value, -1, createdAt);
   }

    public String getValue() {
        return value;
    }

    public long getExp() {
        return exp;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
