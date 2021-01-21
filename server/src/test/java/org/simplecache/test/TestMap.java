package org.simplecache.test;

import java.util.HashMap;
import java.util.Map;

public class TestMap {

    public static void main(String[] args) {
        Map<String, String> nomes = new HashMap<>();

        String result = nomes.put("1", "Hendris");
        System.out.println(result);

        result = nomes.put("1", "Hendris");
        System.out.println(result);
    }
}
