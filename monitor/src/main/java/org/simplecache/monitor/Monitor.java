package org.simplecache.monitor;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * -Djdk.tls.client.protocols=TLSv1.2
 */
public class Monitor {

    private static final Logger LOG = LoggerFactory.getLogger(Monitor.class);

    private final BlockingQueue<Command> queue;
    private final Instances instances;
    private final Dispatcher dispatcher;

    public Monitor() {
        this.queue = new LinkedBlockingQueue<>(50);
        this.instances = new Instances(queue);
        this.dispatcher = new Dispatcher(queue);
    }

    public static void main(String[] args) throws IOException, ApiException {
        new Monitor().run();
    }

    private void run() throws IOException, ApiException {
    /*HttpClient client = HttpClient.newHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:9090/api/v1/namespaces/default/pods?watch=true"))
//                .uri(URI.create("http://localhost:9090/api/v1/namespaces/default/pods"))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    *//*client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(System.out::println)
            .join();*//*

    client.sendAsync(request, HttpResponse.BodyHandlers.ofcons())
            .thenApply(HttpResponse::body)
            .thenAccept(System.out::println)
            .join();*/

        /*ApiClient client = Config.defaultClient();
        client.setVerifyingSsl(false);
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
        for (V1Pod item : list.getItems()) {
            System.out.println(item.getMetadata().getName());
        }*/


        ApiClient client = Config.defaultClient()
                .setReadTimeout(0);

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        /*Watch<V1Namespace> watch = Watch.createWatch(
                client,
                api.listNamespaceCall(null, null, null, null, null, 5, null, null, Boolean.TRUE, null),
                new TypeToken<Watch.Response<V1Namespace>>(){}.getType());

        for (Watch.Response<V1Namespace> item : watch) {
            System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
        }*/

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LOG.info("\n" + instances.toString());
            }
        }).start();

        new Thread(dispatcher).start();

        int iteration = 1;
        while (true) {
            LOG.info("Iteration: {}", iteration++);
            LOG.info("Start: {}", Instant.now());
            long start = System.currentTimeMillis();
            Watch<V1Pod> watchPod = Watch.createWatch(
                    client,
                    api.listNamespacedPodCall("default", null, null, null, null, "app=simplecache", null, null, null, Boolean.TRUE, null),
                    new TypeToken<Watch.Response<V1Pod>>() {
                    }.getType());

            for (Watch.Response<V1Pod> item : watchPod) {
                if (item.status != null && item.status.getMetadata() != null)
                    LOG.info("Continue: {}", item.status.getMetadata().getContinue());
                LOG.info("{} : {} - {} - {}", item.type, item.object.getMetadata().getName(), item.object.getStatus().getPhase(), item.object.getStatus().getPodIP());

                EventType eventType = EventType.valueOf(item.type);
                if (eventType == null) {
                    LOG.info("Ocorreu um erro na identificação do tipo de evento: {}", item.type);
                    continue;
                }

                switch (eventType) {
                    case ADDED:
                    case MODIFIED:
                        instances.addOrModify(item.object.getMetadata().getName(), item.object.getStatus().getPodIP(), item.object.getStatus().getPhase());
                        break;
                    case DELETED:
                        instances.delete(item.object.getMetadata().getName());
                        break;
                    default:
                        break;
                }
            }
            LOG.info("End: {}", Instant.now());
            long end = System.currentTimeMillis();
            long total = end - start;
            LOG.info("Time in seconds: {}", total / 100);
        }
    }
}



