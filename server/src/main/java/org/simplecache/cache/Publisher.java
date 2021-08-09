package org.simplecache.cache;

import org.simplecache.ConnectionManager;

public class Publisher {

    private final ConnectionManager connectionManager;

    public Publisher(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void publish(CacheEntry message) {
        connectionManager.nodes().forEach(serverHandler -> serverHandler.publishSync(message));
    }
}
