package org.simplecache.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.simplecache.cache.Cache;
import org.simplecache.Packet;
import org.simplecache.cache.MessageQueue;

// ClientHandler class
public class ClientHandler implements Runnable {

    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");

    private final Socket socket;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final MessageQueue messageQueue;

    // Constructor
    public ClientHandler(Socket socket, DataInputStream dis, DataOutputStream dos, MessageQueue messageQueue) {
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        String received;
        String toreturn;
        while (true) {
            try {

                // Ask user what he wants
                dos.writeUTF("What do you want?[Date | Time]..\n" +
                        "Type Exit to terminate connection.");

                // receive the answer from client
                received = dis.readUTF();

                if (received.equals("Exit")) {
                    System.out.println("Client " + this.socket + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    break;
                }

                Optional<Packet> packet = toPacket(received);
                if (packet.isPresent()) {
                    process(packet.get());
                } else {
                    // creating Date object
                    Date date = new Date();

                    // write on output stream based on the
                    // answer from the client
                    switch (received) {

                        case "Date":
                            toreturn = fordate.format(date);
                            dos.writeUTF(toreturn);
                            break;

                        case "Time":
                            toreturn = fortime.format(date);
                            dos.writeUTF(toreturn);
                            break;

                        default:
                            dos.writeUTF("Invalid input");
                            break;
                    }
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
            case "SET":
                System.out.println("SET " + packet);
                processSet(packet.getAttributes());
                dos.writeUTF("ok");
                break;
            case "GET":
                System.out.println("GET " + packet);
                String value = processGet(packet.getAttributes());
                Packet packetOut = new Packet();
                packetOut.setCommand("GET_OUT");
                packetOut.add("value", value);
                ObjectMapper objectMapper = new ObjectMapper();
                String output = objectMapper.writeValueAsString(packetOut);
                dos.writeUTF(output);
                break;
            default:
                dos.writeUTF("Invalid input");
                break;
        }
    }

    private String processGet(Map<String, String> attributes) {
        String key = attributes.get("key");
        String value = Cache.INSTANCE.get(key);
        return value;
    }

    private void processSet(Map<String, String> attributes) {
        String key = attributes.get("key");
        String value = attributes.get("value");

        Cache.INSTANCE.set(key, value);

        messageQueue.offer(key + "=" + value);
    }

    public Optional<Packet> toPacket(String received) {
        return Packet.from(received);
    }
}