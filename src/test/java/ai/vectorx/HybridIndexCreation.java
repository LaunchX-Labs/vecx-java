package ai.vectorx;

public class HybridIndexCreation {
  public static void main(String[] args) throws Exception {

    String token = "pankaj:CgYO9pfG4UB1nqLieUMu39HP7HMGnB3w:india-west-1"; // Replace with actual token
    VectorX vx = new VectorX(token);

    String indexName = "hybrid_testing_index";
    int dimension = 128;
    String spaceType = "cosine";
    int vocabSize = 1000;
    int M = 16;
    int efCon = 128;
    boolean useFp16 = true;


    System.out.println(vx.createHybridIndex(
      indexName,
      dimension,
      spaceType,
      vocabSize,
      M,
      efCon,
      useFp16
    ));
  }
}
