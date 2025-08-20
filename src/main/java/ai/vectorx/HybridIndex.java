package ai.vectorx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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

            // Handle dense vector - could be List<Double>, List<Float>, double[], or
            // float[]
            List<Double> denseVector = extractDenseVector(item.get("dense_vector"));

            // Normalize dense vector and get norm
            NormalizationResult normResult = normalizeVector(denseVector);

            // Handle sparse vector
            Map<String, Object> sparseVector = (Map<String, Object>) item.getOrDefault("sparse_vector",
                    new HashMap<>());
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
//        if (!vectorBatch.isEmpty()) {
//            System.out.println("Sample vector structure:");
//            Map<String, Object> sample = vectorBatch.get(0);
//            for (Map.Entry<String, Object> entry : sample.entrySet()) {
//                Object value = entry.getValue();
//                String valueInfo;
//                if (value instanceof List) {
//                    List<?> list = (List<?>) value;
//                    valueInfo = value.getClass().getSimpleName() + " (size=" + list.size() +
//                            ", first few: " + (list.size() > 3 ? list.subList(0, 3) + "..." : list) + ")";
//                } else {
//                    valueInfo = value.getClass().getSimpleName() + " = " + value;
//                }
//                System.out.println("  " + entry.getKey() + ": " + valueInfo);
//            }
//        }

        byte[] serialized;
        String contentType;

        try {
            // Try MessagePack first
            serialized = msgPackMapper.writeValueAsBytes(vectorBatch);
            contentType = "application/msgpack";
            System.out.println("Using MessagePack serialization" + serialized);
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

    private Map<String, Object> decodeMetaFromBase64(String encodedMeta) {
        if (encodedMeta == null || encodedMeta.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            byte[] jsonBytes = Base64.getDecoder().decode(encodedMeta.trim());

            if (jsonBytes.length == 0) {
                return new HashMap<>();
            }

            String jsonString = new String(jsonBytes, StandardCharsets.UTF_8).trim();

            if (jsonString.isEmpty() || jsonString.equals("null")) {
                return new HashMap<>();
            }

            return jsonMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            System.out.println("Warning: Failed to decode/parse metadata: " + e.getMessage());
            return new HashMap<>();
        }
    }

    //
    public List<Map<String, Object>> search(List<Double> denseVector, Map<String, Object> sparseVector,
            int sparseTopK, int denseTopK, boolean includeVectors, int rrfK) throws Exception {

        // Validation
        if (sparseTopK > 256) {
            throw new IllegalArgumentException("sparse_top_k cannot be greater than 256");
        }
        if (denseTopK > 256) {
            throw new IllegalArgumentException("dense_top_k cannot be greater than 256");
        }

        // Normalize dense query vector (ignoring encryption)
        NormalizationResult normalizedDense = normalizeVector(denseVector);

        List<Integer> sparseIndices = (List<Integer>) sparseVector.getOrDefault("indices", new ArrayList<>());
        List<Double> sparseValues = (List<Double>) sparseVector.getOrDefault("values", new ArrayList<>());

        List<Map<String, Object>> sparseQuery = new ArrayList<>();
        for (int i = 0; i < Math.min(sparseIndices.size(), sparseValues.size()); i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("index", sparseIndices.get(i));
            item.put("value", sparseValues.get(i));
            sparseQuery.add(item);
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("dense_vector", normalizedDense.normalizedVector);
        requestData.put("sparse_vector", sparseQuery);
        requestData.put("sparse_top_k", sparseTopK);
        requestData.put("dense_top_k", denseTopK);
        requestData.put("include_vectors", includeVectors);


        String jsonBody = jsonMapper.writeValueAsString(requestData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/hybrid/" + name + "/search_separate"))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Send request
        HttpResponse<String> response = apiClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("HTTP request failed with status: " + response.statusCode() +
                    ", body: " + response.body());
        }

        // Parse response
        JsonNode results = jsonMapper.readTree(response.body());

        // Process results
        Map<String, Object> processedResults = processSearchResults(results, includeVectors);

        // Validate RRF input (simplified validation)
        if (!validateRrfInput(processedResults)) {
            throw new IllegalArgumentException("RRF validation failed");
        }

        // Apply RRF fusion
        List<Map<String, Object>> fusedResults = reciprocalRankFusion(processedResults, rrfK);

        // Clean up vector data if not requested
        if (!includeVectors) {
            for (Map<String, Object> result : fusedResults) {
                result.remove("vector");
            }
        }

        return fusedResults;
    }

    private List<Map<String, Object>> reciprocalRankFusion(Map<String, Object> data, int k) {
        List<Map<String, Object>> denseResults = (List<Map<String, Object>>) data.getOrDefault("dense_results",
                new ArrayList<>());
        List<Map<String, Object>> sparseResults = (List<Map<String, Object>>) data.getOrDefault("sparse_results",
                new ArrayList<>());
        List<Map<String, Object>> metadata = (List<Map<String, Object>>) data.getOrDefault("metadata",
                new ArrayList<>());

        // Create dictionaries for quick lookup
        Map<String, Integer> denseRankMap = new HashMap<>();
        Map<String, Integer> sparseRankMap = new HashMap<>();

        for (Map<String, Object> doc : denseResults) {
            denseRankMap.put((String) doc.get("id"), (Integer) doc.get("rank"));
        }

        for (Map<String, Object> doc : sparseResults) {
            sparseRankMap.put((String) doc.get("id"), (Integer) doc.get("rank"));
        }

        // Create lookup maps for scores and vectors
        Map<String, Map<String, Object>> denseDataMap = new HashMap<>();
        Map<String, Map<String, Object>> sparseDataMap = new HashMap<>();

        for (Map<String, Object> doc : denseResults) {
            denseDataMap.put((String) doc.get("id"), doc);
        }

        for (Map<String, Object> doc : sparseResults) {
            sparseDataMap.put((String) doc.get("id"), doc);
        }

        // Create metadata lookup
        Map<String, Object> metadataMap = new HashMap<>();
        for (Map<String, Object> meta : metadata) {
            metadataMap.put((String) meta.get("id"), meta.getOrDefault("meta", ""));
        }

        // Get all unique document IDs
        Set<String> allDocIds = new HashSet<>();
        allDocIds.addAll(denseRankMap.keySet());
        allDocIds.addAll(sparseRankMap.keySet());

        // Calculate RRF scores for each document
        List<Map<String, Object>> rrfResults = new ArrayList<>();

        for (String docId : allDocIds) {
            double rrfScore = 0.0;

            // Get ranks (null if document doesn't appear in a ranking list)
            Integer denseRank = denseRankMap.get(docId);
            Integer sparseRank = sparseRankMap.get(docId);

            // Calculate RRF contribution from dense ranking
            if (denseRank != null) {
                rrfScore += 1.0 / (k + denseRank);
            }

            // Calculate RRF contribution from sparse ranking
            if (sparseRank != null) {
                rrfScore += 1.0 / (k + sparseRank);
            }

            // Determine which vector to use (prefer dense if both present, otherwise use
            // available)
            Object vector = null;
            if (denseDataMap.containsKey(docId) && sparseDataMap.containsKey(docId)) {
                // Both present, use dense vector
                vector = denseDataMap.get(docId).get("vector");
            } else if (denseDataMap.containsKey(docId)) {
                // Only dense present
                vector = denseDataMap.get(docId).get("vector");
            } else if (sparseDataMap.containsKey(docId)) {
                // Only sparse present
                vector = sparseDataMap.get(docId).get("vector");
            }

            // Create result object
            Map<String, Object> result = new HashMap<>();
            result.put("id", docId);
            result.put("rrf_score", rrfScore);
            result.put("sparse_rank", sparseRank != null ? sparseRank : 0);
            result.put("dense_rank", denseRank != null ? denseRank : 0);
            result.put("meta", metadataMap.getOrDefault(docId, ""));
            result.put("vector", vector);

            rrfResults.add(result);
        }

        // Sort by RRF score in descending order
        rrfResults.sort((a, b) -> Double.compare((Double) b.get("rrf_score"), (Double) a.get("rrf_score")));

        return rrfResults;
    }

    private boolean validateRrfInput(Map<String, Object> data) {
        /**
         * Validate the input data structure for RRF algorithm.
         *
         * @param data Input data to validate
         * @return true if valid, false otherwise
         */

        if (data == null) {
            System.err.println("Input data must not be null");
            return false;
        }

        // Check required keys
        String[] requiredKeys = { "dense_results", "sparse_results" };
        for (String key : requiredKeys) {
            if (!data.containsKey(key)) {
                System.err.println("Missing required key: " + key);
                return false;
            }
        }

        // Validate dense_results structure
        Object denseResultsObj = data.get("dense_results");
        if (!(denseResultsObj instanceof List)) {
            System.err.println("dense_results must be a list");
            return false;
        }

        List<?> denseResults = (List<?>) denseResultsObj;
        for (int i = 0; i < denseResults.size(); i++) {
            Object doc = denseResults.get(i);
            if (!(doc instanceof Map)) {
                System.err.println("dense_results[" + i + "] must be a map");
                return false;
            }

            Map<?, ?> docMap = (Map<?, ?>) doc;
            // Required keys - vector is optional
            String[] requiredDocKeys = { "id", "score", "rank" };
            for (String key : requiredDocKeys) {
                if (!docMap.containsKey(key)) {
                    System.err.println("dense_results[" + i + "] missing required key: " + key);
                    return false;
                }
            }
        }

        // Validate sparse_results structure
        Object sparseResultsObj = data.get("sparse_results");
        if (!(sparseResultsObj instanceof List)) {
            System.err.println("sparse_results must be a list");
            return false;
        }

        List<?> sparseResults = (List<?>) sparseResultsObj;
        for (int i = 0; i < sparseResults.size(); i++) {
            Object doc = sparseResults.get(i);
            if (!(doc instanceof Map)) {
                System.err.println("sparse_results[" + i + "] must be a map");
                return false;
            }

            Map<?, ?> docMap = (Map<?, ?>) doc;
            // Required keys - vector is optional
            String[] requiredDocKeys = { "id", "score", "rank" };
            for (String key : requiredDocKeys) {
                if (!docMap.containsKey(key)) {
                    System.err.println("sparse_results[" + i + "] missing required key: " + key);
                    return false;
                }
            }
        }

        // Validate metadata if present
        if (data.containsKey("metadata")) {
            Object metadataObj = data.get("metadata");
            if (!(metadataObj instanceof List)) {
                System.err.println("metadata must be a list");
                return false;
            }

            List<?> metadata = (List<?>) metadataObj;
            for (int i = 0; i < metadata.size(); i++) {
                Object meta = metadata.get(i);
                if (!(meta instanceof Map)) {
                    System.err.println("metadata[" + i + "] must be a map");
                    return false;
                }

                Map<?, ?> metaMap = (Map<?, ?>) meta;
                if (!metaMap.containsKey("id")) {
                    System.err.println("metadata[" + i + "] missing required key: id");
                    return false;
                }
            }
        }

        return true;
    }

    private Map<String, Object> processSearchResults(JsonNode results, boolean includeVectors) throws Exception {
        Map<String, Object> processed = new HashMap<>();

        // Process dense results
        JsonNode denseNode = results.get("dense_results");
        List<Map<String, Object>> denseResults = new ArrayList<>();
        if (denseNode != null && denseNode.isArray()) {
            for (JsonNode result : denseNode) {
                Map<String, Object> searchResult = new HashMap<>();
                searchResult.put("id", result.get("id").asText());
                searchResult.put("score", result.get("score").asDouble());
                searchResult.put("rank", result.get("rank").asInt());
                searchResult.put("vector", null);

                // Handle vector if present (ignoring decryption)
                if (includeVectors && result.has("vector") && !result.get("vector").isNull()) {
                    searchResult.put("vector", jsonMapper.convertValue(result.get("vector"), List.class));
                }

                denseResults.add(searchResult);
            }
        }
        processed.put("dense_results", denseResults);

        // Process sparse results
        JsonNode sparseNode = results.get("sparse_results");
        List<Map<String, Object>> sparseResults = new ArrayList<>();
        if (sparseNode != null && sparseNode.isArray()) {
            for (JsonNode result : sparseNode) {
                Map<String, Object> searchResult = new HashMap<>();
                searchResult.put("id", result.get("id").asText());
                searchResult.put("score", result.get("score").asDouble());
                searchResult.put("rank", result.get("rank").asInt());
                searchResult.put("vector", null);

                // Handle vector if present (ignoring decryption)
                if (includeVectors && result.has("vector") && !result.get("vector").isNull()) {
                    searchResult.put("vector", jsonMapper.convertValue(result.get("vector"), List.class));
                }

                sparseResults.add(searchResult);
            }
        }
        processed.put("sparse_results", sparseResults);

        // Process metadata
        JsonNode metadataNode = results.get("metadata");
        List<Map<String, Object>> metadata = new ArrayList<>();

        if (metadataNode != null && metadataNode.isArray()) {
            for (JsonNode metaItem : metadataNode) {
                Map<String, Object> metaResult = new HashMap<>();
                metaResult.put("id", metaItem.get("id").asText());

                // Decode the individual meta field
                if (metaItem.has("meta") && !metaItem.get("meta").isNull()) {
                    String encodedMeta = metaItem.get("meta").asText();
                    System.out.println("Encoded Meta: " + encodedMeta);
                    try {
                        Map<String, Object> decodedMeta = decodeMetaFromBase64(encodedMeta);
                        metaResult.put("meta", decodedMeta);
                    } catch (Exception e) {
                        System.out.println("Warning: Failed to decode metadata for " + metaItem.get("id").asText()
                                + ": " + e.getMessage());
                        metaResult.put("meta", new HashMap<>());
                    }
                } else {
                    metaResult.put("meta", new HashMap<>());
                }

                metadata.add(metaResult);
            }
        }
        processed.put("metadata", metadata);

        return processed;
    }

    public Map<String, Object> getVector(String vectorId) throws Exception {
        /**
         * Get a hybrid vector by ID
         *
         * @param vectorId The ID of the vector to retrieve
         * @return Map containing the vector data
         * @throws Exception if the request fails
         */

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/hybrid/" + name + "/vector/" + vectorId))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = apiClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("HTTP request failed with status: " + response.statusCode() +
                    ", body: " + response.body());
        }

        Map<String, Object> result = jsonMapper.readValue(response.body(),
                jsonMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));

        // Process the result (ignoring encryption as requested)

        // Decode metadata for unencrypted mode
        if (result.containsKey("meta") && result.get("meta") != null) {
            try {
                String metaStr = (String) result.get("meta");
                // Assuming base64 decoded content is JSON - simplified approach
                byte[] decodedMeta = Base64.getDecoder().decode(metaStr);
                String decodedJson = new String(decodedMeta, StandardCharsets.UTF_8);

                // Parse the JSON metadata
                Map<String, Object> metaMap = jsonMapper.readValue(decodedJson,
                        jsonMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
                result.put("meta", metaMap);
            } catch (Exception e) {
                System.out.println("Warning: Failed to decode metadata: " + e.getMessage());
                result.put("meta", new HashMap<>());
            }
        }

        // Format sparse vector for consistency
        if (result.containsKey("sparse_vector") && result.get("sparse_vector") != null) {
            Object sparseVectorObj = result.get("sparse_vector");

            if (sparseVectorObj instanceof List) {
                List<?> sparseList = (List<?>) sparseVectorObj;

                // Convert from [{"index": idx, "value": val}] to {"indices": [...], "values":
                // [...]}
                if (!sparseList.isEmpty() && sparseList.get(0) instanceof Map) {
                    List<Integer> indices = new ArrayList<>();
                    List<Double> values = new ArrayList<>();

                    for (Object item : sparseList) {
                        Map<?, ?> itemMap = (Map<?, ?>) item;
                        if (itemMap.containsKey("index") && itemMap.containsKey("value")) {
                            indices.add(((Number) itemMap.get("index")).intValue());
                            values.add(((Number) itemMap.get("value")).doubleValue());
                        }
                    }

                    Map<String, Object> formattedSparse = new HashMap<>();
                    formattedSparse.put("indices", indices);
                    formattedSparse.put("values", values);
                    result.put("sparse_vector", formattedSparse);
                }
            }
            // If it's already in the correct format (Map with indices/values), leave it as
            // is
        }

        return result;
    }

    public String deleteVector(String vectorId) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/hybrid/" + name + "/vector/" + vectorId))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .DELETE()
                .build();

        HttpResponse<String> response = apiClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("HTTP request failed with status: " + response.statusCode() +
                    ", body: " + response.body());
        }

        return "Hybrid vector " + vectorId + " deleted successfully";
    }

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