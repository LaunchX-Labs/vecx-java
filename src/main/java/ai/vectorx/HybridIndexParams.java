package ai.vectorx;

public class HybridIndexParams {
    private String libToken;
    private int totalElements;
    private String spaceType;
    private int dimension;
    private int vocabSize;
    private boolean useFp16;
    private int M;

    // ✅ Constructor
    public HybridIndexParams(String libToken, int totalElements, String spaceType, int dimension,int vocabSize, boolean useFp16, int M) {
        this.libToken = libToken;
        this.totalElements = totalElements;
        this.spaceType = spaceType;
        this.dimension = dimension;
        this.vocabSize = vocabSize;
        this.useFp16 = useFp16;
        this.M = M;
    }

    // ✅ Getters
    public String getLibToken() { return libToken; }
    public int getTotalElements() { return totalElements; }
    public String getSpaceType() { return spaceType; }
    public int getDimension() { return dimension; }
    public int getVocabSize() { return vocabSize; }
    public boolean isUseFp16() { return useFp16; }
    public int getM() { return M; }
}
