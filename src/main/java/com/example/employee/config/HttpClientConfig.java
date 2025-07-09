package com.example.employee.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;

@ApplicationScoped
public class HttpClientConfig {

    private final CloseableHttpClient httpClient;

    public HttpClientConfig() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);

        HttpRequestRetryHandler retryHandler = (exception, executionCount, context) -> {
            if (executionCount > 3) return false;
            return exception instanceof IOException;
        };

        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setRetryHandler(retryHandler)
                .build();
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }
}
