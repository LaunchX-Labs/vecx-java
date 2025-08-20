package ai.vectorx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.google.gson.Gson;
import java.net.URI;
import java.time.Duration;
import java.util.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.zip.Deflater;

import static ai.vectorx.Utils.jsonZip;


public class Index {
    private HttpClient apiClient;
    private String name;
    private String key;
    private String token;
    private String url;
    private int version;
    private String libToken;
    private int count;
    private String spaceType;
    private int dimension;
    private String precision;
    private int M;
    private ObjectMapper jsonMapper;

    public Index(String name, String key, String token, String url, int version, IndexParams params) {
        this.name = name;
        this.key = key;
        this.token = token;
        this.url = url;
        this.version = version;
        this.libToken = params.getLibToken();
        this.count = params.getTotalElements();
        this.spaceType = params.getSpaceType();
        this.dimension = params.getDimension();
        this.precision = params.isUseFp16() ? "float16" : "float32";
        this.M = params.getM();

        this.apiClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.jsonMapper = new ObjectMapper();
    }

    private float norm(float[] vector) {
        double norm = 0.0;
        for(float v: vector) norm += v * v;
        norm = Math.sqrt(norm);
        return (float) norm;
    }

    private float[] normalizeVector(float[] vector, float norm) {
        if(vector.length != this.dimension) {
            throw new IllegalArgumentException("Vector dimension mismatch: expected " + this.dimension + ", got " + vector.length);
        }

        if(!"cosine".equals(this.spaceType)) {
            return vector;
        }

        if(norm == 0.0) return vector;
        float[] normalizedVector = new float[vector.length];

        for(int i = 0; i  < vector.length; i++){
            normalizedVector[i] = (float)(vector[i]/norm);
        }
        return normalizedVector;
    }


    public String upsert(List<Map<String, Object>> inputArray) throws Exception {
        if(inputArray.size() > 1000) {
            throw new IllegalArgumentException("Cannot insert more than 1000 vectors at a time");
        }
        System.out.println(inputArray);

        List<List<Object>> vectorBatch = new ArrayList<>();

        for(Map<String, Object> item : inputArray) {
            float[] vector = (float[]) item.get("vector");
            float norm = norm(vector);
            float[] normalizedVector = normalizeVector(vector, norm);

            List<Float> vectorList = new ArrayList<>();

            for(float v: normalizedVector) {
                vectorList.add(v);
            }

            Map<String, Object> meta = (Map<String, Object>) item.getOrDefault("meta", new HashMap<>());
            Map<String, Object> filter = (Map<String, Object>) item.getOrDefault("filter", new HashMap<>());
            byte[] metaData = jsonZip(meta);
//            System.out.println();
            List<Object> vectorObj = new ArrayList<>();
            vectorObj.add(item.getOrDefault("id", ""));
            vectorObj.add(metaData);  // will be serialized as JSON
            vectorObj.add(filter); // "
            vectorObj.add(norm);    // norm placeholder
            vectorObj.add(vectorList);
            vectorBatch.add(vectorObj);
        }

//        System.out.println("VectorBatch: \n"+vectorBatch);

        ObjectMapper mapper = new ObjectMapper(new MessagePackFactory());

        byte[] serialized = mapper.writeValueAsBytes(vectorBatch);
        System.out.println("serialized: " + serialized);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/index/" + name + "/vector/insert"))
                .header("Authorization", this.token)
                .header("Content-Type", "application/msgpack") // Or use application/msgpack if needed
                .POST(HttpRequest.BodyPublishers.ofByteArray(serialized))
                .build();

        HttpResponse<String> response = apiClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Error in inserting vector" + response.body());
            throw new RuntimeException("Failed with code: " + response.statusCode());
        }

        return response.body();
    }
}