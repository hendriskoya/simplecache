package org.simplecache.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.simplecache.Packet;

public class CacheProtocol {

    public static Optional<Packet> toPacket(String received) {
        return Packet.fromJson(received);
    }

    public static String createSetCache(CacheEntry cacheEntry) {
        Packet packet = new Packet();
        packet.setCommand("SET");
        packet.add("key", cacheEntry.getKey());
        packet.add("value", cacheEntry.getValue());
        packet.add("exp", String.valueOf(cacheEntry.getExp()));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(packet);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String createSyncConfirmationRequest(int cacheHashCode) {
        Packet packet = new Packet();
        packet.setCommand("SYNC_CONFIRMATION");
        packet.add("hashCode", String.valueOf(cacheHashCode));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(packet);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String createSyncConfirmationResponse(Boolean sync) {
        Packet packet = new Packet();
        packet.setCommand("SYNC_CONFIRMATION");
        packet.add("sync", String.valueOf(sync));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(packet);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
