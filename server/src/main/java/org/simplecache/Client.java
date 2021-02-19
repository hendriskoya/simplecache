package org.simplecache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.simplecache.cache.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(Client.class);

    private final String ip;
    private final Environment environment;
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final ObjectMapper objectMapper;


    public Client(String ip) {
        LOG.info("InstanceIp: {}", ip);
        Objects.requireNonNull(ip, "InstanceIp should be not null");
        this.ip = ip;
        this.environment = new Environment();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        LOG.info("Attempt to establish connection with {}", ip);

        Socket socket = null;
        int attempt = 1;
        while (true) {
            if (stop.get()) {
                break;
            }
            try {
                Thread.sleep(1000);
                LOG.info("Attempt {} to connect to node {}", attempt++, ip);
                socket = new Socket(ip, environment.getServerPort());
                LOG.info("Connection established to node {}", ip);
                break;
            } catch (IOException e) {
                LOG.info("Failed to connect to node {}", ip);
//                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {
            try {
                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                // establish the connection with server

                // request sent by server control - init
                String jsonRequest = dis.readUTF();
                Packet request = Packet.fromJson(jsonRequest).orElseThrow(() -> new Exception("Invalid request"));
                LOG.info("request from server control: {}", request);
                // request sent by server control - end

                // response to server control - init
                Packet response = new Packet();
                response.setCommand("IDENTIFICATION");
                response.add("hostname", PodInfo.getHostname());
                response.add("ip", PodInfo.getIp());
                response.add("createdAt", PodInfo.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                String jsonResponse = objectMapper.writeValueAsString(response);
                dos.writeUTF(jsonResponse);
                // response to server control - end

                // request sent by instance handler - init
                /*jsonRequest = dis.readUTF();
                LOG.info("request from instance handler: {}", jsonRequest);
                request = objectMapper.readValue(jsonRequest, Packet.class);
                LOG.info("request converted to object {}", request);*/
                // request sent by instance handler - end

                if (request.getCommand() != null && "IDENTIFICATION".equals(request.getCommand())) {

                    String message = request.getAttributes().get("message");
                    LOG.info(message);


                    Packet packet = new Packet();
                    packet.setCommand("IDENTITY");
                    packet.add("hostname", PodInfo.getHostname());
                    packet.add("ip", PodInfo.getIp());
                    packet.add("createdAt", PodInfo.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    packet.add("type", "CLIENT");
                    jsonResponse = objectMapper.writeValueAsString(packet);

                    dos.writeUTF(jsonResponse);

                    // the following loop performs the exchange of
                    // information between client and client handler
                    while (true) {

                        long start = System.currentTimeMillis();

                        /******************/
                        //                teste de cache
                        //                setCache(dis, dos, "name", "Hendris");
                        //                getCache(dis, dos, "name");

                        /****************/
                        //aguardando o server passar atualização de cache
                        String msgFromServer = dis.readUTF();
                        LOG.info("Message from server: {}", msgFromServer);

                        /**
                         * código temporário (substituir por um protocolo)
                         */
                        String[] split = msgFromServer.split("=");
                        SimpleCache.INSTANCE.set(split[0], split[1]);

                        // If client sends exit,close this connection
                        // and then break from the while loop
                        if (stop.get()) {
                            LOG.info("Closing this connection: " + socket);
                            //                    s.close();
                            LOG.info("Connection closed");
                            break;
                        }

                        //                dos.writeUTF("Update over add/remove cache");

                        // printing date or time as requested by client
                        //                String received = dis.readUTF();
                        //                System.out.println(received);

                        long end = System.currentTimeMillis();

                        long time = end - start;
                        LOG.info("Time: " + time);
                    }

                    // closing resources
                    //            scn.close();
                    dis.close();
                    dos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        LOG.info("Exiting Thread");
    }

    public void doStop() {
        this.stop.set(true);
    }

    public static void setCache(DataInputStream dis, DataOutputStream dos, String key, String value) {
        try {
            Packet packet = new Packet();
            packet.setCommand("SET");
            packet.add("key", key);
            packet.add("value", value);
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(packet);
            dos.writeUTF(json);
            String received = dis.readUTF();
            System.out.println(received);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getCache(DataInputStream dis, DataOutputStream dos, String key) {
        try {
            Packet packet = new Packet();
            packet.setCommand("GET");
            packet.add("key", key);
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(packet);
            dos.writeUTF(json);
            String received = dis.readUTF();
            System.out.println(received);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}