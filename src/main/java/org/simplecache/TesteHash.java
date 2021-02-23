package org.simplecache;

import java.util.HashMap;
import java.util.Map;

public class TesteHash {

    public static void main(String[] args) {
        Map<String, String> dados = new HashMap<>();

        /*dados.put("hendris", "eu");
        dados.put("daniela", "ela");
        dados.put("vinicius", "filho");*/

        System.out.println(dados.hashCode());

        Map<String, String> dados1 = new HashMap<>();

        /*dados1.put("hendris", "eu");
        dados1.put("daniela", "ela");
        dados1.put("vinicius", "filho");*/

        System.out.println(dados1.hashCode());
    }
}
