package org.simplecache;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.StopWatch;

public class CacheClient implements DisposableBean {

    private final Logger LOG = LoggerFactory.getLogger(CacheClient.class);

    private final String host = "192.168.99.100";
    private final Integer port = 31717;

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public CacheClient() {
        LOG.info("Trying connect to the simplecache ...");
        while (true) {
            try {
                socket = new Socket(host, port);
                // obtaining input and out streams
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());

                String ack = dis.readUTF();
                if (ack == null || ack.equalsIgnoreCase("NOT_READY")) {
                    throw new NodeIsNotReadyException();
                }

                LOG.info("Connection to the simplecache is established");
                break;
            } catch (ConnectException e) {
                LOG.error(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                //            throw new RuntimeException("Não foi possível estabelecer conexão com o cache", e);
            } catch (NodeIsNotReadyException e) {
                LOG.error("SimpleCache is not ready to accept connections");
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        //sending event to simplecache to close the connection
        //colocar tratamento aqui para não gerar erro caso a conexão tenha caido
        dos.writeUTF("Exit");

        // closing resources
        if (dos != null)
            dos.close();
        if (dis != null)
            dis.close();
        if (socket != null)
            socket.close();
        LOG.info("Connection to the simplecache is closed");
    }

    public void set(String key, String value) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            Packet packet = new Packet();
            packet.setCommand("SET");
            packet.add("key", key);
            packet.add("value", value);
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(packet);
            dos.writeUTF(json);
            String received = dis.readUTF();
            LOG.info("Received after set: {}", received);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopWatch.stop();
        LOG.info("set key={}, value={}, time={}", key, value, stopWatch.getTotalTimeMillis());
    }

    public String get(String key) {
        LOG.info("isBound: {}", socket.isBound());
        LOG.info("isClosed: {}", socket.isClosed());
        LOG.info("isConnected: {}", socket.isConnected());
        LOG.info("isInputShutdown: {}", socket.isInputShutdown());
        LOG.info("isOutputShutdown: {}", socket.isOutputShutdown());

        try {
            Packet request = new Packet();
            request.setCommand("GET");
            request.add("key", key);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonRequest = objectMapper.writeValueAsString(request);

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            dos.writeUTF(jsonRequest);
            String jsonResponse = dis.readUTF();
            LOG.info("json response: " + jsonResponse);

            stopWatch.stop();

            Packet response = objectMapper.readValue(jsonResponse, Packet.class);
            String value = response.getAttributes().get("value");
            LOG.info("get key={}, value={}, time={}", key, value, stopWatch.getTotalTimeMillis());
            return value;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
