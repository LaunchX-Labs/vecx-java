package com.vectorx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorx.VectorX;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            String token = "pankaj:CgYO9pfG4UB1nqLieUMu39HP7HMGnB3w:india-west-1"; // Replace with actual token
            VectorX vx = new VectorX(token);

//            System.out.println(vx.createHybridIndex(
//                    "hybrid_testing_index",
//                    4,
//                    "cosine",
//                    4,
//                    16,
//                    128,
//                    true
//            ));

//            System.out.println(vx.listIndexes());

//            System.out.println(vx.deleteHybridIndex("hybrid_testing_index"));

            HybridIndex hyidx = vx.getHybridIndex("hybrid_testing_index");
//            List<Map<String, Object>> inputArray = new ArrayList<>();
//
//            Map<String, Object> vector1 = new HashMap<>();
//            vector1.put("id", "vector4");
//            vector1.put("dense_vector", Arrays.asList(0.1, 0.2, 0.3, 0.4));
//
//            Map<String, Object> sparseVector1 = new HashMap<>();
//            sparseVector1.put("indices", Arrays.asList(0, 2, 4));
//            sparseVector1.put("values", Arrays.asList(0.1, 0.5, 0.3));
//            vector1.put("sparse_vector", sparseVector1);
//
//            Map<String, Object> meta1 = new HashMap<>();
//            meta1.put("category", "example");
//            meta1.put("timestamp", System.currentTimeMillis());
//            vector1.put("meta", meta1);
//
//            Map<String, Object> vector2 = new HashMap<>();
//            vector2.put("id", "vector3");
//            vector2.put("dense_vector", Arrays.asList(0.1, 0.2, 0.3, 0.4));
//
//            Map<String, Object> sparseVector2 = new HashMap<>();
//            sparseVector2.put("indices", Arrays.asList(0, 2, 4));
//            sparseVector2.put("values", Arrays.asList(0.1, 0.5, 0.3));
//            vector2.put("sparse_vector", sparseVector1);
//
//            Map<String, Object> meta2 = new HashMap<>();
//            meta2.put("category", "example");
//            meta2.put("timestamp", System.currentTimeMillis());
//            vector2.put("meta", meta2);
//
//            inputArray.add(vector2);
//
//            String result = hyidx.upsert(inputArray);
//            System.out.println("Result: " + result);

            List<Double> denseVector = Arrays.asList(0.1,0.2,0.3,0.4);
            Map<String, Object> sparseVector = new HashMap<>();
            List<Integer> indices = Arrays.asList(10, 25, 100, 250); // vocabulary indices
            List<Double> values = Arrays.asList(0.8, 0.6, 0.9, 0.4);  // corresponding weights
            sparseVector.put("indices", indices);
            sparseVector.put("values", values);

            // 3. Set search parameters
            int sparseTopK = 50;        // Top 50 results from sparse search
            int denseTopK = 50;         // Top 50 results from dense search
            boolean includeVectors = true;  // Include vectors in response
            int rrfK = 60;

            Map<String, Object> filter = new HashMap<>();


            System.out.println(hyidx.getVector("vector1"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
