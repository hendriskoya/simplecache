package org.simplecache.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Esse CACHE precisa ser concorrente já que várias conexões farão uso
 */
public final class Cache {

    private Map<String, String> data;

    public static final Cache INSTANCE = new Cache();

    public Cache() {
        this.data = new HashMap<>();
    }

    public void set(String key, String value) {
        String put = data.put(key, value);
        System.out.println(put);
    }

    public String get(String key) {
        return data.get(key);
    }
}
