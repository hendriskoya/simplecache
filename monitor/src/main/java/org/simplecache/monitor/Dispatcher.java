package org.simplecache.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import org.simplecache.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dispatcher implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(Dispatcher.class);

    private final BlockingQueue<Command> queue;

    public Dispatcher(BlockingQueue<Command> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
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


            // the following loop performs the exchange of
            // information between client and client handler
            System.out.println(dis.readUTF());
            dos.writeUTF("MONITOR");

            while (true) {
                Command command = queue.take();
                if (command.getInstance().isLocalhost()) {
                    LOG.info("COMMAND IGNORED: {}", command);
                } else {
                    LOG.info("COMMAND: {}", command);
                    Packet packet = new Packet();
                    packet.setCommand(command.getInstruction().name());
                    packet.add("instance", command.getInstance().getIp());

                    ObjectMapper objectMapper = new ObjectMapper();
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
        }
    }
}
