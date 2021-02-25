package org.simplecache.handler.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.ehcache.Cache;
import org.simplecache.Packet;
import org.simplecache.PodInfo;
import org.simplecache.cache.CacheProtocol;
import org.simplecache.cache.CacheValue;
import org.simplecache.cache.CacheEntry;
import org.simplecache.cache.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeWorker implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(NodeWorker.class);

    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Socket socket;
    private final BlockingQueue<CacheEntry> messages;
    private final String hostname;
    private final String ip;
    private final LocalDateTime createdAt;
    private final AtomicBoolean stop = new AtomicBoolean(false);

    public NodeWorker(Socket socket, DataInputStream dis, DataOutputStream dos, String hostname, String ip, LocalDateTime createdAt) {
        this.socket = socket;
        this.dis = dis;
        this.dos = dos;
        this.hostname = hostname;
        this.ip = ip;
        this.createdAt = createdAt;
        this.messages = new LinkedBlockingQueue<>();
    }

    public void publish(CacheEntry message) {
        messages.offer(message);
    }

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public void run() {
        //node é mais velho que o nó que solicitou a conexão, portanto, será enviado uma cópia do cache
        if (PodInfo.getCreatedAt().isBefore(createdAt)) {
            LOG.info("Starting sync cache with host {}", hostname);
            Iterator<Cache.Entry<String, CacheValue>> data = SimpleCache.INSTANCE.data();
            while (data.hasNext()) {
                Cache.Entry<String, CacheValue> next = data.next();
                try {
                    CacheValue cacheValue = next.getValue();
                    String jsonMessage = CacheProtocol.createSetCache(new CacheEntry(next.getKey(),
                            cacheValue.getValue(),
                            cacheValue.getExp()));

                    dos.writeUTF(jsonMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            LOG.info("Sync cache is finished with host {}", hostname);

            LOG.info("Validating sync with host {}", hostname);
            String syncConfirmationRequest = CacheProtocol.createSyncConfirmationRequest(SimpleCache.INSTANCE.cacheHashCode());
            try {
                dos.writeUTF(syncConfirmationRequest);
                String jsonResponse = dis.readUTF();
                LOG.info("Response confirmation: " + jsonResponse);
                Packet response = Packet.fromJson(jsonResponse).orElseThrow();
                if (response.getCommand().equalsIgnoreCase("SYNC_CONFIRMATION")) {
                    Boolean sync = Boolean.valueOf(response.getAttributes().get("sync"));
                    if (sync) {
                        LOG.info("Sync with host {} has finished successfully", hostname);
                    } else {
                        LOG.info("Sync with host {} has finished incorrect", hostname);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            while (true) {
                try {
                    // aguardando receber mensagem de cache para enviar para os clientes
                    CacheEntry message = messages.poll(200, TimeUnit.MILLISECONDS);
//                    LOG.info("Enviando mensagem para o cliente: {}", message);

                    if (stop.get()) {
                        LOG.info("Closing this connection: " + socket);
                        break;
                    }

                    if (message != null) {
                        String jsonMessage = CacheProtocol.createSetCache(message);
                        dos.writeUTF(jsonMessage);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

    public void doStop() {
        this.stop.set(true);
    }

    @Override
    public String toString() {
        return "InstanceHandler{" +
                "hostname='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}