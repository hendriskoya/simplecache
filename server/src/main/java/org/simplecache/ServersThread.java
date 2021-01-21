package org.simplecache;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.simplecache.handler.ServerHandler;

public class ServersThread {

    private final ConcurrentMap<String, ServerHandler> servers;

    public ServersThread() {
        this.servers = new ConcurrentHashMap<>();
    }

    public void add(String id, ServerHandler serverHandler) {
        this.servers.put(id, serverHandler);
    }

    public Collection<ServerHandler> get() {
        return servers.values();
    }
}
