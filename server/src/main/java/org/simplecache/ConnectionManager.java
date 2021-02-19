package org.simplecache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.simplecache.handler.server.MonitorHandler;
import org.simplecache.handler.server.NodeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManager {

    private final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);

    private Map<String, Client> clients = new ConcurrentHashMap<>();
    private Map<String, NodeHandler> nodes = new ConcurrentHashMap<>();
    private MonitorHandler monitorHandler;

    public void newClient(String hostname, String ip) {
        LOG.info("New client: {} - {}", hostname, ip);

        /*try {
            InetAddress inet = InetAddress.getByName(ip);
            LOG.info("Sending Ping Request to " + ip);
            LOG.info(inet.isReachable(6100) ? "Host is reachable" : "Host is NOT reachable");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Client client = new Client(ip);
        clients.put(hostname, client);
        Thread thread = new Thread(client);
        thread.setName("client-" + ip);
        thread.start();
    }

    /*public void newInstance(String hostname, String ip) {

    }*/

    public void stop(String hostname, String ip) {
        LOG.info("Attempt to stop client {} - {}", hostname, ip);
        Client client = clients.remove(hostname);
        if (client != null) {
            client.doStop();
        }

        LOG.info("Attempt to stop instance {} - {}", hostname, ip);
        NodeHandler instance = nodes.remove(hostname);
        if (instance != null) {
            instance.doStop();
        }
    }

    public Collection<NodeHandler> nodes() {
        return nodes.values();
    }

    public boolean hasConnectedNodes() {
        return !nodes.isEmpty();
    }

    public void addNode(NodeHandler nodeHandler) {
        nodes.put(nodeHandler.getHostname(), nodeHandler);
    }

    public void setMonitor(MonitorHandler monitorHandler) {
        this.monitorHandler = monitorHandler;
    }

    public boolean isMonitorConnected() {
        return this.monitorHandler != null && this.monitorHandler.isConnected();
    }
}
