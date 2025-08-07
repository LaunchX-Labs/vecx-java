package com.vectorx;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HybridIndex {
    private HttpClient apiClient;
    private String name;
    private String token;
    private String url;
    private int version;
    private String libToken;
    private int count;
    private String spaceType;
    private int dimension;
    private int vocabSize;
    private String precision;
    private int M;
    private ObjectMapper msgPackMapper;
    private ObjectMapper jsonMapper;

    public HybridIndex(String name, String token, String url, int version, HybridIndexParams params) {
        this.name = name;
        this.token = token;
        this.url = url;
        this.version = version;
        this.libToken = params.getLibToken();
        this.count = params.getTotalElements();
        this.spaceType = params.getSpaceType();
        this.dimension = params.getDimension();
        this.precision = params.isUseFp16() ? "float16" : "float32";
        this.vocabSize = params.getVocabSize();
        this.M = params.getM();

        this.apiClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Properly configure MessagePack mapper
        this.msgPackMapper = new ObjectMapper(new MessagePackFactory());
        this.jsonMapper = new ObjectMapper();
    }

    public String upsert(List<Map<String, Object>> inputArray) throws Exception {
        if (inputArray.size() > 1000) {
            throw new IllegalArgumentException("Cannot insert more than 1000 vectors at a time");
        }

        List<Map<String, Object>> vectorBatch = new ArrayList<>();

        for (Map<String, Object> item : inputArray) {
            String vectorId = String.valueOf(item.getOrDefault("id", ""));

            // Handle dense vector - could be List<Double>, List<Float>, double[], or float[]
            List<Double> denseVector = extractDenseVector(item.get("dense_vector"));

            // Normalize dense vector and get norm
            NormalizationResult normResult = normalizeVector(denseVector);

            // Handle sparse vector
            Map<String, Object> sparseVector = (Map<String, Object>) item.getOrDefault("sparse_vector", new HashMap<>());
            List<Integer> indices = extractIntegerList(sparseVector.get("indices"));
            List<Double> values = extractDoubleList(sparseVector.get("values"));

            // Handle metadata
            Map<String, Object> meta = (Map<String, Object>) item.getOrDefault("meta", new HashMap<>());
            String metaB64 = encodeMetaToBase64(meta);

            // Create hybrid vector object matching Python structure exactly
            Map<String, Object> hybridVector = new LinkedHashMap<>();
            hybridVector.put("id", vectorId);

            // Convert to float list to match Python's tolist() behavior
            List<Float> denseVectorFloats = normResult.normalizedVector.stream()
                    .map(Double::floatValue)
                    .collect(Collectors.toList());
            hybridVector.put("dense_vector", denseVectorFloats);

            hybridVector.put("indices", indices);

            // Convert values to float list
            List<Float> valuesFloats = values.stream()
                    .map(Double::floatValue)
                    .collect(Collectors.toList());
            hybridVector.put("values", valuesFloats);

            hybridVector.put("dense_norm", (float) normResult.norm);
            hybridVector.put("meta", metaB64);

            vectorBatch.add(hybridVector);
        }

        // Debug: Print the structure of the first vector
        if (!vectorBatch.isEmpty()) {
            System.out.println("Sample vector structure:");
            Map<String, Object> sample = vectorBatch.get(0);
            for (Map.Entry<String, Object> entry : sample.entrySet()) {
                Object value = entry.getValue();
                String valueInfo;
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    valueInfo = value.getClass().getSimpleName() + " (size=" + list.size() +
                            ", first few: " + (list.size() > 3 ? list.subList(0, 3) + "..." : list) + ")";
                } else {
                    valueInfo = value.getClass().getSimpleName() + " = " + value;
                }
                System.out.println("  " + entry.getKey() + ": " + valueInfo);
            }
        }

        byte[] serialized;
        String contentType;

        try {
            // Try MessagePack first
            serialized = msgPackMapper.writeValueAsBytes(vectorBatch);
            contentType = "application/msgpack";
            System.out.println("Using MessagePack serialization");
        } catch (Exception e) {
            System.err.println("MessagePack serialization failed, using JSON fallback: " + e.getMessage());
            // Fallback to JSON if MessagePack fails
            serialized = jsonMapper.writeValueAsBytes(vectorBatch);
            contentType = "application/json";

            // Print JSON for debugging
            String jsonStr = jsonMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(vectorBatch.size() > 0 ? Arrays.asList(vectorBatch.get(0)) : vectorBatch);
            System.out.println("Sample JSON being sent:");
            System.out.println(jsonStr);
        }

        return makeRequest(serialized, contentType);
    }

    private String makeRequest(byte[] serialized, String contentType) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/hybrid/" + name + "/add"))
                .header("Authorization", token)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(serialized))
                .build();

        HttpResponse<String> response = apiClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            System.err.println("Error in inserting vector: " + response.body());
            System.err.println("Request URL: " + request.uri());
            System.err.println("Content-Type: " + contentType);
            System.err.println("Data size: " + serialized.length + " bytes");
            throw new RuntimeException("Failed with code: " + response.statusCode() + ", body: " + response.body());
        }

        return "Hybrid vectors inserted successfully";
    }

    private List<Double> extractDenseVector(Object denseVectorObj) {
        if (denseVectorObj == null) {
            return new ArrayList<>();
        }

        if (denseVectorObj instanceof List) {
            List<?> list = (List<?>) denseVectorObj;
            return list.stream()
                    .map(this::convertToDouble)
                    .collect(Collectors.toList());
        } else if (denseVectorObj instanceof double[]) {
            double[] array = (double[]) denseVectorObj;
            return Arrays.stream(array).boxed().collect(Collectors.toList());
        } else if (denseVectorObj instanceof float[]) {
            float[] array = (float[]) denseVectorObj;
            return IntStream.range(0, array.length)
                    .mapToDouble(i -> array[i])
                    .boxed()
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Unsupported dense vector format: " + denseVectorObj.getClass());
        }
    }

    private List<Integer> extractIntegerList(Object obj) {
        if (obj == null) {
            return new ArrayList<>();
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            return list.stream()
                    .map(item -> {
                        if (item instanceof Number) {
                            return ((Number) item).intValue();
                        } else if (item instanceof String) {
                            try {
                                return Integer.parseInt((String) item);
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Cannot convert string to integer: " + item);
                            }
                        }
                        throw new IllegalArgumentException("Cannot convert to integer: " + item);
                    })
                    .collect(Collectors.toList());
        } else if (obj instanceof int[]) {
            int[] array = (int[]) obj;
            return Arrays.stream(array).boxed().collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<Double> extractDoubleList(Object obj) {
        if (obj == null) {
            return new ArrayList<>();
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            return list.stream()
                    .map(this::convertToDouble)
                    .collect(Collectors.toList());
        } else if (obj instanceof double[]) {
            double[] array = (double[]) obj;
            return Arrays.stream(array).boxed().collect(Collectors.toList());
        } else if (obj instanceof float[]) {
            float[] array = (float[]) obj;
            return IntStream.range(0, array.length)
                    .mapToDouble(i -> array[i])
                    .boxed()
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private Double convertToDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert string to double: " + obj);
            }
        }
        throw new IllegalArgumentException("Cannot convert to double: " + obj);
    }

    // Normalization result class
    private static class NormalizationResult {
        List<Double> normalizedVector;
        double norm;

        NormalizationResult(List<Double> normalizedVector, double norm) {
            this.normalizedVector = normalizedVector;
            this.norm = norm;
        }
    }

    // Normalize vector and return both normalized vector and norm
    private NormalizationResult normalizeVector(List<Double> vector) {
        if (vector.isEmpty()) {
            return new NormalizationResult(new ArrayList<>(), 0.0);
        }

        // Calculate L2 norm
        double norm = Math.sqrt(vector.stream()
                .mapToDouble(Double::doubleValue)
                .map(x -> x * x)
                .sum());

        if (norm == 0.0) {
            return new NormalizationResult(new ArrayList<>(vector), 0.0);
        }

        // Normalize vector
        List<Double> normalizedVector = vector.stream()
                .map(x -> x / norm)
                .collect(Collectors.toList());

        return new NormalizationResult(normalizedVector, norm);
    }

    // Encode metadata to Base64
    private String encodeMetaToBase64(Map<String, Object> meta) throws Exception {
        byte[] jsonBytes = jsonMapper.writeValueAsBytes(meta);
        return Base64.getEncoder().encodeToString(jsonBytes);
    }

    // Test method to validate the structure being sent
//    public void testStructure(List<Map<String, Object>> inputArray) throws Exception {
//        if (inputArray.isEmpty()) {
//            System.out.println("Input array is empty");
//            return;
//        }
//
//        System.out.println("=== Testing Structure ===");
//        Map<String, Object> item = inputArray.get(0);
//
//        String vectorId = String.valueOf(item.getOrDefault("id", ""));
//        List<Double> denseVector = extractDenseVector(item.get("dense_vector"));
//        NormalizationResult normResult = normalizeVector(denseVector);
//
//        Map<String, Object> sparseVector = (Map<String, Object>) item.getOrDefault("sparse_vector", new HashMap<>());
//        List<Integer> indices = extractIntegerList(sparseVector.get("indices"));
//        List<Double> values = extractDoubleList(sparseVector.get("values"));
//
//        Map<String, Object> meta = (Map<String, Object>) item.getOrDefault("meta", new HashMap<>());
//        String metaB64 = encodeMetaToBase64(meta);
//
//        Map<String, Object> hybridVector = new LinkedHashMap<>();
//        hybridVector.put("id", vectorId);
//
//        List<Float> denseVectorFloats = normResult.normalizedVector.stream()
//                .map(Double::floatValue)
//                .collect(Collectors.toList());
//        hybridVector.put("dense_vector", denseVectorFloats);
//
//        hybridVector.put("indices", indices);
//
//        List<Float> valuesFloats = values.stream()
//                .map(Double::floatValue)
//                .collect(Collectors.toList());
//        hybridVector.put("values", valuesFloats);
//
//        hybridVector.put("dense_norm", (float) normResult.norm);
//        hybridVector.put("meta", metaB64);
//
//        // Print the structure
//        String json = jsonMapper.writerWithDefaultPrettyPrinter()
//                .writeValueAsString(Arrays.asList(hybridVector));
//        System.out.println("Generated structure:");
//        System.out.println(json);
//        System.out.println("=== End Test ===");
//    }

    public Map<String, Object> describe() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        data.put("space_type", this.spaceType);
        data.put("dimension", this.dimension);
        data.put("count", this.count);
        data.put("precision", this.precision);
        data.put("M", this.M);
        data.put("vocab_size", this.vocabSize);
        return data;
    }
}