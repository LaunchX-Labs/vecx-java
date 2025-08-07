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
//            vector1.put("id", "vector2");
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
//            inputArray.add(vector1);
//
//            String result = hyidx.upsert(inputArray);
//            System.out.println("Result: " + result);
            System.out.println(hyidx.describe());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
