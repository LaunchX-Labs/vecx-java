package ai.vectorx;

public class DeleteIndex {
    public static void main(String[] args) throws Exception {
        String token = "pankaj:CgYO9pfG4UB1nqLieUMu39HP7HMGnB3w:india-west-1"; // Replace with actual token
        VectorX vx = new VectorX(token);

        vx.deleteHybridIndex("hybrid_testing_index");
    }
}
