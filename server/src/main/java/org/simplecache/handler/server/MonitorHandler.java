package org.simplecache.handler.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Optional;
import org.simplecache.ConnectionManager;
import org.simplecache.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorHandler implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(MonitorHandler.class);

    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Socket socket;
    private final ConnectionManager connectionManager;

    public MonitorHandler(Socket socket, DataInputStream dis, DataOutputStream dos, ConnectionManager connectionManager) {
        LOG.info("Establishing a monitor connection...");
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        this.connectionManager = connectionManager;
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

                Optional<Packet> packet = Packet.fromJson(received);
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
                connectionManager.newClient(packet.getAttributes().get("hostname"), packet.getAttributes().get("ip"));
                break;
            case "REMOVE":
                LOG.info("REMOVE " + packet);
                dos.writeUTF("ok");
                connectionManager.stop(packet.getAttributes().get("hostname"), packet.getAttributes().get("ip"));
                break;
            default:
                dos.writeUTF("Invalid input");
                break;
        }
    }

    public boolean isConnected() {
        return socket.isConnected();
    }
}