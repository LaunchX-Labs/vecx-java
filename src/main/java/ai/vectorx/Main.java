package ai.vectorx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        try {
            String token = "pankaj:CgYO9pfG4UB1nqLieUMu39HP7HMGnB3w:india-west-1"; // Replace with actual token
            VectorX vx = new VectorX(token);

            String indexName = "testing_index";
            int dim = 8;
            String spaceType = "cosine";
            boolean useFP16 = true;
            int eConf = 128;
            int M = 16;

            Index index = vx.getIndex("testing_index");

            List<Map<String,Object>> vector_array = new ArrayList<>();

            Map<String,Object> vector_map = new HashMap<>();

            vector_map.put("id", "doc_1");
            float[] vector = {0.1F,0.2F,0.3F,0.4F,0.5F,0.6F,0.6F,0.7F};
            vector_map.put("vector", vector);
            Map<String, Object> meta = new HashMap<>();
            meta.put("text", "Hi there");
            meta.put("id", "doc_1");
            vector_map.put("meta", meta);

            Map<String, Object> filter = new HashMap<>();
            filter.put("tag", "important");
            filter.put("group", 7);

            vector_map.put("filter", filter);

            vector_array.add(vector_map);

            System.out.println(index.upsert(vector_array));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
