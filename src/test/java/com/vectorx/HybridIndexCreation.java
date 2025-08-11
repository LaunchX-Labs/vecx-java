package com.vectorx;

public class HybridIndexCreation {
    public static void main(String[] args) throws Exception {

        String token = "pankaj:CgYO9pfG4UB1nqLieUMu39HP7HMGnB3w:india-west-1"; // Replace with actual token
        VectorX vx = new VectorX(token);

        System.out.println(vx.createHybridIndex(
                "hybrid_testing_index",
                128,
                "cosine",
                1000,
                16,
                128,
                true
        ));
    }
}
