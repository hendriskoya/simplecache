package org.simplecache.handler.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.simplecache.ConnectionManager;
import org.simplecache.Environment;
import org.simplecache.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeHandler implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(NodeWorker.class);

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final ConnectionManager connectionManager;

    public NodeHandler(Environment environment, ConnectionManager connectionManager) {
        this.environment = environment;
        this.connectionManager = connectionManager;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(environment.getServerPort());
            LOG.info("Listening on port {}", environment.getServerPort());

            while (true) {
                LOG.info("Waiting new instance...");
                Socket socket = null;
                try {
                    //                    waitingToBeReady();

                    socket = serverSocket.accept();

                    // obtaining input and out streams
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                    LOG.info("Assigning new thread for this instance");

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

                    LOG.info("A new instance server is connected: {}", socket);
                    String id = "server-" + ip;
                    NodeWorker nodeWorker = new NodeWorker(socket, dis, dos, hostname, ip, createdAt);
                    connectionManager.addNode(nodeWorker);
                    Thread worker = new Thread(nodeWorker, id);

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
