package org.simplecache.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(ServerHandler.class);

    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Socket socket;
    private final BlockingQueue<String> messages;

//    private ServerHandler() {}

    public ServerHandler(Socket socket, DataInputStream dis, DataOutputStream dos) {
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        this.messages = new LinkedBlockingQueue<>();
    }

    public void publish(String message) {
        messages.offer(message);
    }

    @Override
    public void run() {
        String received;
        String toreturn;
        try {
            // Ask user what he wants
            dos.writeUTF("Hi Server. What is you IP?");
            // receive the answer from client
            received = dis.readUTF();
            System.out.println("Instance: " + received);
            while (true) {
                try {
                    // aguardando receber mensagem de cache para enviar para os clientes
                    String message = messages.take();

                    LOG.info("Enviando mensagem para o cliente: {}", message);

                    dos.writeUTF(message);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                /*} catch (SocketException e) {
                    LOG.error("Provavelmente a conexão com outro server foi perdida", e);
                    break;*/
                }
            }
        } catch (IOException e) {
            LOG.error("Erro de IO na comunicação entre as instâncias do server", e);
        }

        try {
            // closing resources
            this.dis.close();
            this.dos.close();
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            LOG.error("Erro na tentativa de fechar o socket", e);
        }
    }

/*    public Optional<Packet> toPacket(String received) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return Optional.of(objectMapper.readValue(received, Packet.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }*/
}