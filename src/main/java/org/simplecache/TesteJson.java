package org.simplecache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class TesteJson {

    public static void main(String[] args) throws JsonProcessingException {
        Packet packet = new Packet();

        packet.setCommand("GET");
        packet.add("key", "name");
        packet.add("value", "Hendris");

        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(packet);
        System.out.println(s);
    }
}

