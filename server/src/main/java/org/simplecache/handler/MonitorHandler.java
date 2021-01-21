package org.simplecache.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;
import org.simplecache.cache.Cache;
import org.simplecache.ClientManager;
import org.simplecache.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorHandler implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(MonitorHandler.class);

    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Socket socket;
    private final ClientManager clientManager;

    public MonitorHandler(Socket socket, DataInputStream dis, DataOutputStream dos, ClientManager clientManager) {
        LOG.info("Establishing a monitor connection...");
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        this.clientManager = clientManager;
    }

    @Override
    public void run() {
        String received;
        String toreturn;
        while (true) {
            try {

                // receive the answer from client
                received = dis.readUTF();

                LOG.info("Received: {}", received);

                Optional<Packet> packet = Packet.from(received);
                if (packet.isPresent()) {
                    process(packet.get());
                }
            } catch (SocketException e) {
                e.printStackTrace();
                System.out.println("Provavelmente a conexão com o client foi perdida");
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            // closing resources
            this.dis.close();
            this.dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(Packet packet) throws IOException {
        //aqui deve aplicar um padrão de projeto como Command
        String command = packet.getCommand();
        switch (command) {
            case "ADD":
                LOG.info("ADD " + packet);
//                processSet(packet.getAttributes());
                dos.writeUTF("ok");
                clientManager.newClient(packet.getAttributes().get("instance"));
                break;
            case "REMOVE":
                LOG.info("REMOVE " + packet);
                dos.writeUTF("ok");
                clientManager.stop(packet.getAttributes().get("instance"));
                break;
            default:
                dos.writeUTF("Invalid input");
                break;
        }
    }
}