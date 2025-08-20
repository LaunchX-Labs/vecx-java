package ai.vectorx;

import java.util.*;

public class SearchTest {
  public static void main(String[] args) throws Exception {
    VectorX vx = new VectorX("pankaj:CgYO9pfG4UB1nqLieUMu39HP7HMGnB3w:india-west-1");
    HybridIndex index = vx.getHybridIndex("new_testing_index");

    System.out.println(index.describe());


    System.out.println(vx.listIndexes());

    List<Double> denseVector4 = new ArrayList<>();
    denseVector4.addAll(Arrays.asList(0.2, 0.3, 0.4, 0.5, 0.6));
    for (int i = 0; i < 123; i++) {
      denseVector4.add(0.15);
    }
    Map<String, Object> sparseVector4 = new HashMap<>();
    sparseVector4.put("indices", Arrays.asList(10, 25, 50));
    sparseVector4.put("values", Arrays.asList(0.8, 0.6, 0.4));

    int topDenseK = 50;
    int topSparseK = 50;
    boolean includeVectors = false;
    int rrfK = 2;

    System.out.println(index.search(denseVector4, sparseVector4, topSparseK, topDenseK, includeVectors, rrfK));
  }
}
