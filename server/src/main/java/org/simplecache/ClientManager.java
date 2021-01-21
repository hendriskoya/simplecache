package org.simplecache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientManager {

    private final Logger LOG = LoggerFactory.getLogger(ClientManager.class);

    private Map<String, Client> clients = new ConcurrentHashMap<>();

    public void newClient(String instanceIp) {
        Client client = new Client(instanceIp);
        clients.put(instanceIp, client);
        Thread thread = new Thread(client);
        thread.setName("client-" + instanceIp);
        thread.start();
    }

    public void stop(String instanceIp) {
        LOG.info("Attempt to stop instance {}", instanceIp);
        clients.get(instanceIp).doStop();
    }
}
