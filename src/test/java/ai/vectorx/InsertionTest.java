package ai.vectorx;

import java.util.*;

public class InsertionTest {
    public static void main(String[] args) throws Exception {
        VectorX vx = new VectorX("pankaj:CgYO9pfG4UB1nqLieUMu39HP7HMGnB3w:india-west-1");



        List<Map<String,Object>> sampleDocs = new ArrayList<>();


//        doc 1
        Map<String, Object> doc_1 = new HashMap<>();
        doc_1.put("id", "doc_1");

        List<Double> denseVector1 = new ArrayList<>();
        denseVector1.addAll(Arrays.asList(0.1,0.2,0.3,0.4,0.5));
        for( int i = 0; i < 123; i++) {
            denseVector1.add(0.1);
        }
        doc_1.put("dense_vector", denseVector1);

        Map<String,Object> sparseVector1 = new HashMap<>();
        sparseVector1.put("indices", Arrays.asList(10,25,50,100));
        sparseVector1.put("values", Arrays.asList(0.8,0.6,0.4,0.3));
        doc_1.put("sparse_vector", sparseVector1);

        Map<String,Object> meta1 = new HashMap<>();
        meta1.put("title", "Introduction to Machine Learning");
        meta1.put("category", "technology");
        meta1.put("author", "Dr. Smith");
        meta1.put("year", 2023);
        meta1.put("tags", Arrays.asList("AI", "ML", "data science"));
        meta1.put("content_length", 1500);
        meta1.put("popularity_score", 8.5);
        doc_1.put("meta", meta1);

        sampleDocs.add(doc_1);

//        doc 2
        Map<String, Object> doc_2 = new HashMap<>();
        doc_2.put("id", "doc_2");

        List<Double> denseVector2 = new ArrayList<>();
        denseVector2.addAll(Arrays.asList(0.6, 0.7, 0.8, 0.9, 0.2));
        for (int i = 0; i < 123; i++) {
            denseVector2.add(0.2);
        }
        doc_2.put("dense_vector", denseVector2);

        Map<String, Object> sparseVector2 = new HashMap<>();
        sparseVector2.put("indices", Arrays.asList(15, 30, 60, 120));
        sparseVector2.put("values", Arrays.asList(0.9, 0.7, 0.5, 0.2));
        doc_2.put("sparse_vector", sparseVector2);

        Map<String, Object> meta2 = new HashMap<>();
        meta2.put("title", "Advanced Neural Networks");
        meta2.put("category", "technology");
        meta2.put("author", "Prof. Johnson");
        meta2.put("year", 2023);
        meta2.put("tags", Arrays.asList("neural networks", "deep learning"));
        meta2.put("content_length", 2200);
        meta2.put("popularity_score", 9.2);
        doc_2.put("meta", meta2);

        sampleDocs.add(doc_2);

//        doc 3
        Map<String, Object> doc_3 = new HashMap<>();
        doc_3.put("id", "doc_3");

        List<Double> denseVector3 = new ArrayList<>();
        denseVector3.addAll(Arrays.asList(0.3, 0.1, 0.4, 0.6, 0.8));
        for (int i = 0; i < 123; i++) {
            denseVector3.add(0.3);
        }
        doc_3.put("dense_vector", denseVector3);

        // Sparse vector for doc_3
        Map<String, Object> sparseVector3 = new HashMap<>();
        sparseVector3.put("indices", Arrays.asList(20, 40, 80, 150));
        sparseVector3.put("values", Arrays.asList(0.7, 0.8, 0.6, 0.4));
        doc_3.put("sparse_vector", sparseVector3);

        // Metadata for doc_3
        Map<String, Object> meta3 = new HashMap<>();
        meta3.put("title", "Sports Analytics Revolution");
        meta3.put("category", "sports");
        meta3.put("author", "Mike Wilson");
        meta3.put("year", 2023);
        meta3.put("tags", Arrays.asList("analytics", "sports", "statistics"));
        meta3.put("content_length", 1800);
        meta3.put("popularity_score", 7.3);
        doc_3.put("meta", meta3);

        sampleDocs.add(doc_3);

        HybridIndex hyidx = vx.getHybridIndex("hybrid_testing_index");

        System.out.println(hyidx.upsert(sampleDocs));
    }
}
