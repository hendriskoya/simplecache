package org.simplecache;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.simplecache.cache.MessageQueue;
import org.simplecache.cache.SimpleCache;
import org.simplecache.handler.client.ClientHandler;
import org.simplecache.handler.server.MonitorHandler;
import org.simplecache.handler.server.NodeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private final Logger LOG = LoggerFactory.getLogger(Server.class);

    private final Environment environment;

    private final ConnectionManager connectionManager;

//    private final InstancesThread instancesThread;

    private final MessageQueue messageQueue;

    private final ObjectMapper objectMapper;

    private final CoreV1Api k8sProxyApi;

    public Server() {
        this.environment = new Environment();
        this.connectionManager = new ConnectionManager();
//        this.instancesThread = new InstancesThread();
        this.messageQueue = new MessageQueue();
        this.objectMapper = new ObjectMapper();

//        ApiClient client = null;
        try {
//            client = Config.defaultClient();
            Configuration.setDefaultApiClient(Config.defaultClient());
        } catch (IOException e) {
//            e.printStackTrace();
            throw new RuntimeException(e);
        }
        k8sProxyApi = new CoreV1Api();
//        Configuration.setDefaultApiClient(client);
    }

    public static void main(String[] args) throws IOException {
        new Server().run();
    }

    private void run() throws IOException {
        startLoggerThread();

        startMessageQueueWorker();

        startServerControl();

        startClientControl();
    }

    private void startMessageQueueWorker() {
        MessageQueueWorker messageQueueWorker = new MessageQueueWorker(connectionManager, messageQueue);
        Thread t = new Thread(messageQueueWorker, "messageQueueWorker");
        t.start();
    }

    private void startClientControl() throws IOException {
        ServerSocket serverSocket = new ServerSocket(environment.getClientPort());
        System.out.println("Listening on port " + environment.getClientPort());

        int client = 1;
        while (true) {
            System.out.println("Waiting new client...");
            Socket socket = null;
            try {
//                waitingToBeReady();

                socket = serverSocket.accept();
                System.out.println("A new client is connected : " + socket);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                if (isNotReady()) {
                    dos.writeUTF("NOT_READY");
                    dos.close();
                    dis.close();
                    socket.close();
                    continue;
                }
                dos.writeUTF("READY");

                System.out.println("Assigning new thread for this client");

                String id = "client-" + client;
                ClientHandler clientHandler = new ClientHandler(socket, dis, dos, messageQueue);
                // create a new thread object
                Thread t = new Thread(clientHandler, id);
                client++;

                // Invoking the start() method
                t.start();
            } catch (Exception e) {
                if (socket != null)
                    socket.close();
                e.printStackTrace();
            }
        }
    }

    private void waitingToBeReady() throws ApiException {
        LOG.info("Waiting to be ready");
//        ApiClient client = Config.defaultClient();
//        Configuration.setDefaultApiClient(client);

//        CoreV1Api api = new CoreV1Api();
        V1PodList list = k8sProxyApi.listPodForAllNamespaces(null, null, null, "app=simplecache", null, null, null, null, null);
        List<Node> nodes = new ArrayList<>();
        System.out.println("Nodes info from k8s proxy");
        for (V1Pod item : list.getItems()) {
            System.out.println("Name: " + item.getMetadata().getName());
            Node node = new Node(item.getMetadata().getName(), item.getMetadata().getCreationTimestamp());
            nodes.add(node);
        }

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (nodes.size() == 1 && PodInfo.getHostname().equals(nodes.get(0))) {
                LOG.info("Nodes Size = 1 / equals");
                return;
            }
        }
    }

    class Node {

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
    }

    private void startServerControl() throws IOException {
        ServerSocket serverSocket = new ServerSocket(environment.getServerPort());
        LOG.info("Listening on port {}", environment.getServerPort());
        Thread tControl = new Thread(() -> {
            while (true) {
                LOG.info("Waiting new instance or monitor...");
                Socket socket = null;
                try {
//                    waitingToBeReady();

                    socket = serverSocket.accept();

                    // obtaining input and out streams
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                    LOG.info("Assigning new thread for this instance or monitor");

                    // send request to monitor/client - init
                    Packet request = new Packet();
                    request.setCommand("IDENTIFICATION");
                    request.add("message", "Hi. Who are you?");
                    String jsonRequest = objectMapper.writeValueAsString(request);
                    LOG.info("jsonRequest: {}", jsonRequest);
                    dos.writeUTF(jsonRequest);
                    // send request to monitor/client - end

                    // response from monitor/client - init
                    String jsonResponse = dis.readUTF();
                    LOG.info("jsonResponse: {}", jsonResponse);
                    Packet response = Packet.fromJson(jsonResponse).orElseThrow(() -> new Exception("Invalid response"));

                    String hostname = response.getAttributes().get("hostname");
                    String ip = response.getAttributes().get("ip");
                    LocalDateTime createdAt = LocalDateTime.parse(response.getAttributes().get("createdAt"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    String type = response.getAttributes().get("type");

                    LOG.info("response info: {}, {}, {}, {}", hostname, ip, createdAt, type);
                    // response from monitor/client - end

                    Thread worker;
                    if (type != null && type.equals("MONITOR")) {
                        LOG.info("The monitor is connected: {}", socket);
                        MonitorHandler monitorHandler = new MonitorHandler(socket, dis, dos, connectionManager);
                        connectionManager.setMonitor(monitorHandler);
                        worker = new Thread(monitorHandler, "monitor");
                    } else {
                        LOG.info("A new instance server is connected: {}", socket);
                        String id = "server-" + ip;
                        NodeHandler nodeHandler = new NodeHandler(socket, dis, dos, hostname, ip, createdAt);
                        connectionManager.addNode(nodeHandler);
//                        instancesThread.add(id, instanceHandler);
                        worker = new Thread(nodeHandler, id);
                    }
                    // Invoking the start() method
                    worker.start();

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        // O Close do socket deve ficar na exception porque não pode ser fechado após a execução da thread
                        // já que esses objetos são utilizados por threads que continuam em execução
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        tControl.setName("ThreadControl");
        tControl.start();
    }

    private void startLoggerThread() {
        Thread tCount = new Thread(() -> {
            while (true) {
                System.out.println("\nCurrent active threads: " + Thread.activeCount());
                Set<Thread> threads = Thread.currentThread().getAllStackTraces().keySet();
                String threadGroupName = Thread.currentThread().getThreadGroup().getName();
//                threads.stream().filter(thread -> thread.getThreadGroup().getName().equals(threadGroupName)).forEach(thread -> System.out.println("\t" + thread.getName()));

                String threadsName = threads.stream()
                        .filter(thread -> thread.getThreadGroup().getName().equals(threadGroupName))
                        .map(thread -> thread.getName())
                        .sorted()
                        .collect(Collectors.joining(", "));
                System.out.println("\t" + threadsName);

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("\nInstances: " + connectionManager.nodes().size());

                Set<Instance> instances = new TreeSet<>();

                instances.addAll(connectionManager.nodes().stream().map(it -> new Instance(it.getHostname(), it.getIp(), it.getCreatedAt())).collect(Collectors.toList()));
                instances.add(new Instance(PodInfo.getHostname(), PodInfo.getIp(), PodInfo.getCreatedAt(), true));

                instances.forEach(it -> System.out.println("\t" + it.getHostname() + " - " + it.getIp() + " - " + it.getCreatedAt() + (it.isItSelf() ? " - (self)" : "")));
            }
        });
        tCount.setName("ThreadLogger");
        tCount.start();
    }

    private boolean isNotReady() {
        return !connectionManager.isMonitorConnected() && (!isTheOne() || !SimpleCache.INSTANCE.isSynchronized(connectionManager));
    }

    private boolean isTheOne() {
        boolean isTheOne = !connectionManager.hasConnectedNodes();
        if (isTheOne) {
            LOG.info("Is it The One? Yes, It is The One!");
        } else {
            LOG.info("Is it The One? No. There are connected nodes!");
        }
        return isTheOne;
    }

    class Instance implements Comparable<Instance> {

        private final String hostname;
        private final String ip;
        private final LocalDateTime createdAt;
        private final boolean itSelf;

        Instance(String hostname, String ip, LocalDateTime createdAt) {
            this.hostname = hostname;
            this.ip = ip;
            this.createdAt = createdAt;
            this.itSelf = false;
        }

        Instance(String hostname, String ip, LocalDateTime createdAt, boolean itSelf) {
            this.hostname = hostname;
            this.ip = ip;
            this.createdAt = createdAt;
            this.itSelf = itSelf;
        }

        public String getHostname() {
            return hostname;
        }

        public String getIp() {
            return ip;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public boolean isItSelf() {
            return itSelf;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Instance instance = (Instance) o;
            return createdAt.equals(instance.createdAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(createdAt);
        }

        @Override
        public int compareTo(Instance o) {
            return getCreatedAt().compareTo(o.getCreatedAt());
        }
    }
}