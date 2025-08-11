package ai.vectorx;

import java.net.http.HttpClient;
import java.time.Duration;

public class ApiClient {
    private final HttpClient client;
    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public HttpClient getClient() {
        return client;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
