package org.simplecache;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import org.apache.commons.lang.time.StopWatch;
import org.simplecache.cache.MessageQueue;
import org.simplecache.cache.SimpleCache;
import org.simplecache.handler.client.ClientHandler;
import org.simplecache.handler.server.NodeHandler;
import org.simplecache.monitor.MonitorWorker;
import org.simplecache.monitor.Nodes;
import org.simplecache.worker.LoggerWorker;
import org.simplecache.worker.MessageQueueWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private final Logger LOG = LoggerFactory.getLogger(Server.class);

    private final Environment environment;

    private final ConnectionManager connectionManager;

    private final MessageQueue messageQueue;

    private final ObjectMapper objectMapper;

    private final CoreV1Api k8sProxyApi;

    private final Nodes nodes;

    private final int nodesOnStartup;

    public Server() {
        this.environment = new Environment();
        this.connectionManager = new ConnectionManager();
        this.messageQueue = new MessageQueue();
        this.objectMapper = new ObjectMapper();

        try {
            Configuration.setDefaultApiClient(Config.defaultClient().setReadTimeout(0));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        k8sProxyApi = new CoreV1Api();

        this.nodes = new Nodes(connectionManager);
        this.nodesOnStartup = checkForExistingNodes();
    }

    public static void main(String[] args) throws IOException {
        new Server().run();
    }

    private void run() throws IOException {
        startLoggerWorker();

        startMessageQueueWorker();

        startMonitorWorker();

        startNodeHandler();

        startClientHandler();
    }

    private void startLoggerWorker() {
        Thread thread = new Thread(new LoggerWorker(connectionManager), "LoggerWorker");
        thread.start();
    }

    private void startMessageQueueWorker() {
        MessageQueueWorker messageQueueWorker = new MessageQueueWorker(connectionManager, messageQueue);
        Thread t = new Thread(messageQueueWorker, "MessageQueueWorker");
        t.start();
    }

    private void startMonitorWorker() {
        MonitorWorker monitorWorker = new MonitorWorker(k8sProxyApi, nodes);
        Thread t = new Thread(monitorWorker, "MonitorWorker");
        t.start();
    }

    private void startNodeHandler() {
        Thread thread = new Thread(new NodeHandler(environment, connectionManager), "NodeHandler");
        thread.start();
    }

    private void startClientHandler() throws IOException {
        ClientHandler clientHandler = new ClientHandler(environment, connectionManager, messageQueue, nodesOnStartup);
        clientHandler.start();
    }

    private int checkForExistingNodes() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        LOG.info("Checking for existing nodes");
        StringBuilder output = new StringBuilder();
        output.append("\nList of found nodes:");
        int result = 0;
        try {

            V1PodList list = k8sProxyApi.listPodForAllNamespaces(null, null, null, "app=simplecache", null, null, null, null, null);
            output.append("\n\tNodes: ");
            output.append(list.getItems().size());
            for (V1Pod item : list.getItems()) {
                output.append("\n\tHostname: ");
                output.append(item.getMetadata().getName());
                output.append(", IP: ");
                output.append(item.getStatus().getPodIP());
                output.append(", Status: ");
                output.append(item.getStatus().getPhase());

//                nodes.addOrModify(item.getMetadata().getName(), item.getStatus().getPodIP(), item.getStatus().getPhase());
            }
            result = list.getItems().size();
            LOG.info(output.toString());
        } catch (ApiException e) {
            e.printStackTrace();
        }

        stopWatch.stop();
        LOG.info("Total time of checking for existing nodes: {}", stopWatch.getTime());
        return result;
    }

    /*class Node {

        private final String name;
        private final LocalDateTime createdAt;

        Node(String name, DateTime creationTimestamp) {
            this.name = name;
            this.createdAt = creationTimestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        public String getName() {
            return name;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }*/
}