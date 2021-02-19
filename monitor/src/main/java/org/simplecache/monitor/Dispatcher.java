package org.simplecache.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import org.simplecache.Packet;
import org.simplecache.PodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dispatcher implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(Dispatcher.class);

    private final BlockingQueue<Command> queue;
    private final ObjectMapper objectMapper;

    public Dispatcher(BlockingQueue<Command> queue) {
        this.queue = queue;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        LOG.info("Initiating Dispatcher");
        // getting localhost ip
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try (
            // establish the connection with server port 5056
            Socket s = new Socket(ip, 6100);

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream())
        ) {
            // request from server control - init
            String jsonRequest = dis.readUTF();
            Packet request = Packet.fromJson(jsonRequest).orElseThrow(() -> new Exception("Invalid request"));
            LOG.info("Request from server: {}", request);
            // request from server control - end

            // response to server control - init
            Packet response = new Packet();
            response.setCommand("IDENTIFICATION");
            response.add("hostname", PodInfo.getHostname());
            response.add("ip", PodInfo.getIp());
            response.add("createdAt", PodInfo.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.add("type", "MONITOR");

            String jsonResponse = objectMapper.writeValueAsString(response);

            dos.writeUTF(jsonResponse);
            // response to server control - end

            while (true) {
                LOG.info("Waiting inclusion of new instances");
                Command command = queue.take();
                if (command.getInstance().isLocalhost()) {
                    LOG.info("COMMAND IGNORED: {}", command);
                } else {
                    LOG.info("COMMAND: {}", command);
                    Packet packet = new Packet();
                    packet.setCommand(command.getInstruction().name());
                    packet.add("hostname", command.getInstance().getHostname());
                    packet.add("ip", command.getInstance().getIp());

                    String json = objectMapper.writeValueAsString(packet);
                    dos.writeUTF(json);
                    String received = dis.readUTF();
                    LOG.info("Response received: {}", received);
                }
            }

        } catch (InterruptedException | UnknownHostException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.info("Ending Dispatcher");
    }
}
