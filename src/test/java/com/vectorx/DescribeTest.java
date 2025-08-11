package com.vectorx;

public class DescribeTest {
    public static void main(String[] args) throws Exception {
        String token = "pankaj:CgYO9pfG4UB1nqLieUMu39HP7HMGnB3w:india-west-1"; // Replace with actual token
        VectorX vx = new VectorX(token);

        HybridIndex hyidx = vx.getHybridIndex("hybrid_testing_index");

        System.out.println(hyidx.describe());
    }
}
