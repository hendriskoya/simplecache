package org.simplecache;

import static org.junit.Assert.*;

import java.util.Optional;
import org.junit.Test;

public class ClientHandlerTest {

    @Test
    public void toPacket() {
        ClientHandler clientHandler = new ClientHandler(null, null, null);

        String invalid = "Mensagem de teste";
        Optional<Packet> packet = clientHandler.toPacket(invalid);

        System.out.println(packet.isPresent());

        String valid = "{\n" +
                "  \"command\": \"GET\",\n" +
                "  \"attributes\": {\n" +
                "    \"value\": \"Hendris\",\n" +
                "    \"key\": \"name\"\n" +
                "  }\n" +
                "}";

        packet = clientHandler.toPacket(valid);

        System.out.println(packet.isPresent());
    }
}