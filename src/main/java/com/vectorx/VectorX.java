package  com.vectorx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.net.*;
import java.time.Duration;
import java.util.*;



public class VectorX {
    private String token;
    private String region = "local";
    private String baseUrl = "http://127.0.0.1:8080/";
    private HttpClient apiClient;
//    private static final List<String> SUPPORTED_REGIONS = Array.asList("us-west","india-west","local");

    public VectorX (String token) {
        this.token = token;
        if(token != null) {
            String[] tokenParts = token.split(":");
            if(tokenParts.length > 2) {
                this.baseUrl = "https://" + tokenParts[2] + ".vectorxdb.ai/api/v1";
                this.token = tokenParts[0] + ":" + tokenParts[1];
            }
        }

        this.apiClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String createIndex(String name, int dimension, String spaceType, int M, int efCon, boolean useFp16, int version) throws Exception {
        if(!isValidIndexName(name)) {
            throw new IllegalArgumentException("Invalid index name. Index name must be alphanumeric and can contain underscores and less than 48 characters");
        }
        if(dimension > 10000) {
            throw new IllegalArgumentException("Dimension cannot be greater than 10000");
        }

        spaceType = spaceType.toLowerCase();
        if (!Arrays.asList("cosine", "l2", "ip").contains(spaceType)) {
            throw new IllegalArgumentException("Invalid space type: " + spaceType);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("index_name", name);
        data.put("dim", dimension);
        data.put("space_type", spaceType);
        data.put("M", M);
        data.put("ef_con", efCon);
        data.put("checksum", -1);
        data.put("use_fp16", useFp16);
        data.put("version", version);

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(data);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl+"/index/create"))
                .header("Authorization",token)
                .header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = apiClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public String listIndexes() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl+"/index/list"))
                .header("Authorization",token)
                .header("Content-Type","application/json")
                .GET()
                .build();

        HttpResponse<String> response = apiClient.send(request, HttpResponse.BodyHandlers.ofString());



        return response.body();
    }

    public Index getIndex(String name) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl+"/index/"+name+"/info"))
                .header("Authorization",token)
                .header("Content-Type","application/json")
                .GET()
                .build();

        ObjectMapper mapper = new ObjectMapper();

        HttpResponse<String> response = apiClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

        JsonNode node = mapper.readTree(response.body());
        String token = node.get("lib_token").asText();
        int m = node.get("M").asInt();
        boolean useFp16 = node.get("use_fp16").asBoolean();
        int dimension = node.get("dimension").asInt();
        int  efCon = node.get("ef_con").asInt();
        int totalElements =  node.get("total_elements").asInt();
        String spaceType = node.get("space_type").asText();

        IndexParams params = new IndexParams(token,totalElements,spaceType,dimension,useFp16,m);

        return new Index(name,"", this.token, this.baseUrl,1,params);
    }

    public String deleteIndex(String name) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl+"/index/"+name+"/delete"))
                .header("Authorization",token)
                .header("Content-Type","application/json")
                .DELETE()
                .build();

        HttpResponse<String> response = apiClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    private boolean isValidIndexName(String indexname) {
        return indexname.matches("^[a-zA-Z0-9_]{1,48}$");
    }
}