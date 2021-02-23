package org.simplecache.monitor;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.Watch;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * -Djdk.tls.client.protocols=TLSv1.2
 */
public class MonitorWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorWorker.class);

    private final Nodes nodes;
    private final CoreV1Api k8sProxyApi;

    public MonitorWorker(CoreV1Api k8sProxyApi, Nodes nodes) {
        this.k8sProxyApi = k8sProxyApi;
        this.nodes = nodes;
    }

    @Override
    public void run() {
        int iteration = 1;
        while (true) {
            try {
                LOG.info("Iteration number {} started at {}", iteration++, Instant.now());
                long start = System.currentTimeMillis();
                Watch<V1Pod> watchPod = Watch.createWatch(
                        Configuration.getDefaultApiClient(),
                        k8sProxyApi.listNamespacedPodCall("default", null, null, null, null, "app=simplecache", null, null, null, Boolean.TRUE, null),
                        new TypeToken<Watch.Response<V1Pod>>() {
                        }.getType());

                for (Watch.Response<V1Pod> item : watchPod) {
                    LOG.info("{} : {} - {} - {}", item.type, item.object.getMetadata().getName(), item.object.getStatus().getPhase(), item.object.getStatus().getPodIP());

                    EventType eventType = EventType.valueOf(item.type);
                    if (eventType == null) {
                        LOG.warn("Unknown event type \"{}\". This process may not work correctly", item.type);
                        continue;
                    }

                    switch (eventType) {
                        case ADDED:
                        case MODIFIED:
                            nodes.addOrModify(item.object.getMetadata().getName(), item.object.getStatus().getPodIP(), item.object.getStatus().getPhase());
                            break;
                        case DELETED:
                            nodes.delete(item.object.getMetadata().getName());
                            break;
                        default:
                            break;
                    }
                }
                LOG.info("Iteration number {} ended at {}", iteration, Instant.now());
                long end = System.currentTimeMillis();
                long total = end - start;
                LOG.info("Total time in seconds of iteration number {] is  {}", iteration, total / 100);
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }
}



