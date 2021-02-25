package org.simplecache;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public class Controller {

    private final Logger LOG = LoggerFactory.getLogger(Controller.class);

    private final CacheClient cacheClient;

    public Controller(CacheClient cacheClient) {
        this.cacheClient = cacheClient;
    }

    @PostMapping
    public ResponseEntity<String> set(@RequestParam("key") String key, @RequestParam("value") String value, @RequestParam(value = "ttl", required = false) String ttl) {
        LOG.info("POST {} - {}", key, value);
        Duration duration = null;
        if (ttl != null && !ttl.isBlank()) {
            duration = Duration.ofSeconds(Long.valueOf(ttl));
        }
        cacheClient.set(key, value, duration);
        return ResponseEntity.ok(key + " - " + value);
    }

    @GetMapping
    public ResponseEntity<String> get(@RequestParam("key") String key) {
        LOG.info("GET {}", key);
        String value = cacheClient.get(key);
        return ResponseEntity.ok(key + " - " + value);
    }
}
