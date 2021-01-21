package org.simplecache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.simplecache.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(Client.class);

    private final String instanceIp;
    private final Environment environment;
    private AtomicBoolean stop = new AtomicBoolean(false);

    public Client(String instanceIp) {
        LOG.info("InstanceIp: {}", instanceIp);
        Objects.requireNonNull(instanceIp, "InstanceIp should be not null");
        this.instanceIp = instanceIp;
        this.environment = new Environment();
    }

    @Override
    public void run() {
        LOG.info("Attempt to establish connection with {}", instanceIp);

        try (Socket s = new Socket(instanceIp, environment.getServerPort());

             // obtaining input and out streams
             DataInputStream dis = new DataInputStream(s.getInputStream());
             DataOutputStream dos = new DataOutputStream(s.getOutputStream())) {
            // establish the connection with server port 5056


            LOG.info(dis.readUTF());
            dos.writeUTF("I'm instance server");

            LOG.info(dis.readUTF());
            String hostname = System.getenv("HOSTNAME");
            dos.writeUTF("I'm " + hostname);

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
                Cache.INSTANCE.set(split[0], split[1]);

                // If client sends exit,close this connection
                // and then break from the while loop
                if (stop.get()) {
                    LOG.info("Closing this connection: " + s);
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
//            dis.close();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
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