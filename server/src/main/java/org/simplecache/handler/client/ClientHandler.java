package org.simplecache.handler.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.simplecache.ConnectionManager;
import org.simplecache.Environment;
import org.simplecache.cache.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler {

    private final Logger LOG = LoggerFactory.getLogger(ClientHandler.class);

    private final Environment environment;
    private final ConnectionManager connectionManager;
    private final MessageQueue messageQueue;
    private final int nodesOnStartup;

    public ClientHandler(Environment environment, ConnectionManager connectionManager, MessageQueue messageQueue, int nodesOnStartup) {
        this.environment = environment;
        this.connectionManager = connectionManager;
        this.messageQueue = messageQueue;
        this.nodesOnStartup = nodesOnStartup;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(environment.getClientPort());
        LOG.info("Listening on port " + environment.getClientPort());

        int client = 1;
        while (true) {
            LOG.info("Waiting new client...");
            Socket socket = null;
            try {
//                waitingToBeReady();

                socket = serverSocket.accept();
                LOG.info("Client {} is connected", socket);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                if (!isReady()) {
                    LOG.info("Closing connection with {} because node is not read", socket);
                    dos.writeUTF("NOT_READY");
                    dos.close();
                    dis.close();
                    socket.close();
                    continue;
                }
                dos.writeUTF("READY");

                LOG.info("Assigning new thread for this client {}", socket);

                String id = "client-" + client;
                ClientWorker clientWorker = new ClientWorker(socket, dis, dos, messageQueue);
                // create a new thread object
                Thread t = new Thread(clientWorker, id);
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

    private boolean isReady() {
        return isUnique() || (hasAtLeast2Nodes() && isSynchronized());
    }

    private boolean isSynchronized() {
        return connectionManager.allFollowersSynced();
    }

    private boolean hasAtLeast2Nodes() {
        return nodesOnStartup >= 2;
    }

    private boolean isUnique() {
        return nodesOnStartup == 1;
    }
}
