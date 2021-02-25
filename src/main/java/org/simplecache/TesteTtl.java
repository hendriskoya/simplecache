package org.simplecache;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TesteTtl {

    public static void main(String[] args) {
        /*Duration ttl = Duration.of(10, ChronoUnit.SECONDS);

        long exp = Instant.now().plusMillis(ttl.toMillis()).toEpochMilli() / 1000;
        long now = Instant.now().toEpochMilli() / 1000;
        System.out.println(exp);
        System.out.println(now);
        while (true) {
            now = Instant.now().toEpochMilli() / 1000;
            long diff = exp - now;
            System.out.println(diff);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (diff < 0) {
                break;
            }
        }*/

        long t1 = Instant.now().toEpochMilli();
        System.out.println(t1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long t2 = Instant.now().toEpochMilli();
        System.out.println(t2);
    }
}
