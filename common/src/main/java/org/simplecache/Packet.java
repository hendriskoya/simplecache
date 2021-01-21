package org.simplecache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Packet {

    private String command;

    private Map<String, String> attributes = new HashMap<>();

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void add(String key, String value) {
        this.attributes.put(key, value);
    }

    public static Optional<Packet> from(String value) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return Optional.of(objectMapper.readValue(value, Packet.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "Packet{" +
                "command='" + command + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
