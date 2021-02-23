package org.simplecache.monitor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.simplecache.ConnectionManager;
import org.simplecache.PodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nodes {

    private final Logger LOG = LoggerFactory.getLogger(Nodes.class);
    private final String RUNNING_STATUS = "running";

    private final Map<String, Node> instances;
    private final ConnectionManager connectionManager;

    public Nodes(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.instances = new HashMap<>();
    }

    public void addOrModify(String hostname, String ip, String status) {
        if (validate(hostname)) return;

        LOG.info("Attempt to add or modify instance {} with ip {} and status {}", hostname, ip, status);
        if (instances.containsKey(hostname)) {
            LOG.info("Instance {} was modified with IP {} to status {}", hostname, ip, status);
            Node node = instances.get(hostname);
            LOG.info("Current instance {} status is {}", hostname, node.getStatus());
            String previousStatus = node.getStatus();
            node.setIp(ip);
            node.setStatus(status);
            if (!(RUNNING_STATUS.equalsIgnoreCase(status) && status.equalsIgnoreCase(previousStatus))) {
                if (RUNNING_STATUS.equalsIgnoreCase(status)) {
                    connectionManager.newFollower(node.getHostname(), node.getIp());
                }
            }
        } else {
            LOG.info("Instance {} was added with IP {} and status {}", hostname, ip, status);
            instances.put(hostname, new Node(hostname, ip, status));
            if (RUNNING_STATUS.equalsIgnoreCase(status)) {
                connectionManager.newFollower(hostname, ip);
            }
        }
    }

    public void delete(String hostname) {
        if (validate(hostname)) return;

        LOG.info("Attempt to remove instance {}", hostname);
        Node removedNode = instances.remove(hostname);
        LOG.info("Instance {} was removed", removedNode);
        connectionManager.stop(removedNode.getHostname(), removedNode.getIp());
    }

    private boolean validate(String hostname) {
        if (PodInfo.getHostname().equalsIgnoreCase(hostname)) {
            LOG.info("Ignoring localhost {}", PodInfo.getHostname());
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Time: ");
        sb.append(LocalDateTime.now());
        sb.append("\nInstances {");
        instances.forEach((key, value) -> {
            sb.append("\n\t");
            sb.append(value);
        });
        sb.append("\n}");
        return sb.toString();
    }
}