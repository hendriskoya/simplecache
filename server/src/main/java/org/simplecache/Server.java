package org.simplecache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import org.simplecache.cache.MessageQueue;
import org.simplecache.handler.ClientHandler;
import org.simplecache.handler.MonitorHandler;
import org.simplecache.handler.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private final Logger LOG = LoggerFactory.getLogger(Server.class);

    private final Environment environment;

    private final ClientManager clientManager;

    private final ServersThread serversThread;

    private final MessageQueue messageQueue;

    public Server() {
        this.environment = new Environment();
        this.clientManager = new ClientManager();
        this.serversThread = new ServersThread();
        this.messageQueue = new MessageQueue();
    }

    public static void main(String[] args) throws IOException {
        new Server().run();
    }

    private void run() throws IOException {
        startCounterThread();

        startMessageQueueWorker();

        startServerControl();

        startClientControl();
    }

    private void startMessageQueueWorker() {
        MessageQueueWorker messageQueueWorker = new MessageQueueWorker(serversThread, messageQueue);
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
                socket = serverSocket.accept();
                System.out.println("A new client is connected : " + socket);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                System.out.println("Assigning new thread for this client");

                String id = "client-" + client;
                ClientHandler clientHandler = new ClientHandler(socket, dis, dos, messageQueue);
                // create a new thread object
                Thread t = new Thread(clientHandler, id);
                client++;

                // Invoking the start() method
                t.start();
            } catch (Exception e) {
                socket.close();
                e.printStackTrace();
            }
        }
    }

    private void startServerControl() throws IOException {
        ServerSocket serverSocket = new ServerSocket(environment.getServerPort());
        System.out.println("Listening on port " + environment.getServerPort());
        Thread tControl = new Thread(() -> {
            int server = 1;
            while (true) {
                System.out.println("Waiting new instance server...");
                Socket socket = null;
                try {
                    socket = serverSocket.accept();

                    // obtaining input and out streams
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                    System.out.println("Assigning new thread for this instance server");

                    dos.writeUTF("Who are you?");

                    String received = dis.readUTF();
                    LOG.info("received: {}", received);

                    Thread t;
                    if (received != null && received.equals("MONITOR")) {
                        System.out.println("The monitor is connected : " + socket);
                        MonitorHandler monitorHandler = new MonitorHandler(socket, dis, dos, clientManager);
                        t = new Thread(monitorHandler, "monitor");
                    } else {
                        System.out.println("A new instance server is connected : " + socket);
                        String id = "server-" + server;
                        ServerHandler serverHandler = new ServerHandler(socket, dis, dos);
                        serversThread.add(id, serverHandler);
                        t = new Thread(serverHandler, id);
                        server++;
                    }
                    // Invoking the start() method
                    t.start();
                } catch (Exception e) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }
        });
        tControl.setName("ThreadControl");
        tControl.start();
    }

    private void startCounterThread() {
        Thread tCount = new Thread(() -> {
            while (true) {
                System.out.println("Current active threads: " + Thread.activeCount());
                Set<Thread> threads = Thread.currentThread().getAllStackTraces().keySet();
                String threadGroupName = Thread.currentThread().getThreadGroup().getName();
                threads.stream().filter(thread -> thread.getThreadGroup().getName().equals(threadGroupName)).forEach(thread -> System.out.println("\t" + thread.getName()));
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        tCount.setName("ThreadCounter");
        tCount.start();
    }
}

