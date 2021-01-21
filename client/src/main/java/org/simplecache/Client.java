package org.simplecache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try {
            Scanner scn = new Scanner(System.in);

            // getting localhost ip 
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection with server port 5056 
//            Socket s = new Socket(ip, 5959);
            Socket s = new Socket("192.168.99.100", 31717);

            // obtaining input and out streams 
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // the following loop performs the exchange of 
            // information between client and client handler 
            while (true) {
                System.out.println(dis.readUTF());
                String tosend = scn.nextLine();

                long start = System.currentTimeMillis();

                /******************/
//                teste de cache
                setCache(dis, dos, "name", "Hendris");
                getCache(dis, dos, "name");

                /****************/


                dos.writeUTF(tosend);

                // If client sends exit,close this connection  
                // and then break from the while loop 
                if (tosend.equals("Exit")) {
                    System.out.println("Closing this connection : " + s);
                    System.out.println("Connection closed");
                    break;
                }

                // printing date or time as requested by client 
                String received = dis.readUTF();
                System.out.println(received);

                long end = System.currentTimeMillis();

                long time = end - start;
                System.out.println("Time: " + time);
            }

            // closing resources 
            dis.close();
            dos.close();
            s.close();
            scn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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