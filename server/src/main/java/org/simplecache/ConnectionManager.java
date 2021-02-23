package org.simplecache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.simplecache.handler.server.FollowerWorker;
import org.simplecache.handler.server.NodeWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManager {

    private final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private Map<String, FollowerWorker> followers = new ConcurrentHashMap<>();
    private Map<String, NodeWorker> nodes = new ConcurrentHashMap<>();

    public void newFollower(String hostname, String ip) {
        LOG.info("New client: {} - {}", hostname, ip);
        FollowerWorker followerWorker = new FollowerWorker(ip);
        followers.put(hostname, followerWorker);
        Thread thread = new Thread(followerWorker);
        thread.setName("client-" + ip);
        thread.start();
    }

    public void stop(String hostname, String ip) {
        LOG.info("Attempt to stop client {} - {}", hostname, ip);
        FollowerWorker followerWorker = followers.remove(hostname);
        if (followerWorker != null) {
            followerWorker.doStop();
        }

        LOG.info("Attempt to stop instance {} - {}", hostname, ip);
        NodeWorker instance = nodes.remove(hostname);
        if (instance != null) {
            instance.doStop();
        }
    }

    public Collection<NodeWorker> nodes() {
        return nodes.values();
    }

    public boolean allFollowersSynced() {
        boolean synced = true;
        for (FollowerWorker worker: followers.values()) {
            synced = synced && worker.isSynced();
        }
        return synced;
    }

    public boolean hasConnectedNodes() {
        return !nodes.isEmpty();
    }

    public void addNode(NodeWorker nodeWorker) {
        nodes.put(nodeWorker.getHostname(), nodeWorker);
    }
}
