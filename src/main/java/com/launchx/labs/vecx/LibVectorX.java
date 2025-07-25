package com.launchx.labs.vecx;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.logging.Logger;

/**
 * Java client for VectorX - High-performance vector encryption and operations.
 * 
 * This class provides a Java interface to the VectorX C++ library, offering
 * vector encryption, decryption, and similarity calculation operations.
 * 
 * <p>Example usage:
 * <pre>{@code
 * try (LibVectorX vectorX = new LibVectorX()) {
 *     float[] vector = {1.0f, 2.0f, 3.0f, 4.0f};
 *     byte[] encrypted = vectorX.encryptVector(vector);
 *     float[] decrypted = vectorX.decryptVector(encrypted);
 * }
 * }</pre>
 */
public class LibVectorX implements AutoCloseable {
    
    private static final Logger LOGGER = Logger.getLogger(LibVectorX.class.getName());
    
    private final VectorXNative nativeLib;
    private final Pointer instance;
    private boolean closed = false;
    
    /**
     * Creates a new LibVectorX instance.
     * 
     * @throws VectorXException if the native library cannot be loaded or instance creation fails
     */
    public LibVectorX() throws VectorXException {
        try {
            this.nativeLib = LibraryLoader.loadLibrary();
            this.instance = nativeLib.vecx_create();
            
            if (this.instance == null) {
                throw new VectorXException("Failed to create VectorX instance");
            }
            
            LOGGER.info("VectorX instance created successfully");
        } catch (UnsatisfiedLinkError e) {
            throw new VectorXException("Failed to load VectorX native library", e);
        }
    }
    
    /**
     * Encrypts a single vector.
     * 
     * @param vector the vector to encrypt
     * @return encrypted vector data
     * @throws VectorXException if encryption fails
     */
    public byte[] encryptVector(float[] vector) throws VectorXException {
        checkClosed();
        
        FloatBuffer vectorBuffer = BufferUtils.createFloatBuffer(vector);
        int encryptedSize = BufferUtils.estimateEncryptedSize(vector.length * Float.BYTES);
        ByteBuffer encryptedBuffer = BufferUtils.createByteBuffer(encryptedSize);
        
        int result = nativeLib.vecx_encrypt_vector(instance, vectorBuffer, vector.length, 
                                                  encryptedBuffer, encryptedSize);
        
        if (result != 0) {
            String error = getLastError();
            throw new VectorXException("Vector encryption failed: " + error, result);
        }
        
        // Return only the used portion of the buffer
        byte[] encrypted = new byte[encryptedBuffer.position()];
        encryptedBuffer.flip();
        encryptedBuffer.get(encrypted);
        
        return encrypted;
    }
    
    /**
     * Decrypts a single vector.
     * 
     * @param encryptedVector the encrypted vector data
     * @return decrypted vector
     * @throws VectorXException if decryption fails
     */
    public float[] decryptVector(byte[] encryptedVector) throws VectorXException {
        return decryptVector(encryptedVector, -1);
    }
    
    /**
     * Decrypts a single vector with known dimensions.
     * 
     * @param encryptedVector the encrypted vector data
     * @param expectedSize expected size of the decrypted vector (-1 for auto-detect)
     * @return decrypted vector
     * @throws VectorXException if decryption fails
     */
    public float[] decryptVector(byte[] encryptedVector, int expectedSize) throws VectorXException {
        checkClosed();
        
        ByteBuffer encryptedBuffer = BufferUtils.createByteBuffer(encryptedVector);
        
        // Use expected size or estimate from encrypted data
        int vectorSize = expectedSize > 0 ? expectedSize : encryptedVector.length / Float.BYTES;
        FloatBuffer vectorBuffer = BufferUtils.createFloatBuffer(vectorSize);
        
        int result = nativeLib.vecx_decrypt_vector(instance, encryptedBuffer, encryptedVector.length,
                                                  vectorBuffer, vectorSize);
        
        if (result != 0) {
            String error = getLastError();
            throw new VectorXException("Vector decryption failed: " + error, result);
        }
        
        return BufferUtils.toFloatArray(vectorBuffer);
    }
    
    /**
     * Encrypts multiple vectors in batch.
     * 
     * @param vectors array of vectors to encrypt
     * @return encrypted vectors data
     * @throws VectorXException if encryption fails
     */
    public byte[] encryptVectors(float[][] vectors) throws VectorXException {
        checkClosed();
        
        if (vectors == null || vectors.length == 0) {
            throw new IllegalArgumentException("Vectors array cannot be null or empty");
        }
        
        int numVectors = vectors.length;
        int vectorSize = vectors[0].length;
        
        FloatBuffer vectorsBuffer = BufferUtils.createFloatBufferFromVectors(vectors);
        int encryptedSize = BufferUtils.estimateEncryptedSize(numVectors * vectorSize * Float.BYTES);
        ByteBuffer encryptedBuffer = BufferUtils.createByteBuffer(encryptedSize);
        
        int result = nativeLib.vecx_encrypt_vectors(instance, vectorsBuffer, numVectors, vectorSize,
                                                   encryptedBuffer, encryptedSize);
        
        if (result != 0) {
            String error = getLastError();
            throw new VectorXException("Vectors encryption failed: " + error, result);
        }
        
        // Return only the used portion
        byte[] encrypted = new byte[encryptedBuffer.position()];
        encryptedBuffer.flip();
        encryptedBuffer.get(encrypted);
        
        return encrypted;
    }
    
    /**
     * Decrypts multiple vectors in batch.
     * 
     * @param encryptedVectors encrypted vectors data
     * @param numVectors number of vectors
     * @param vectorSize size of each vector
     * @return array of decrypted vectors
     * @throws VectorXException if decryption fails
     */
    public float[][] decryptVectors(byte[] encryptedVectors, int numVectors, int vectorSize) throws VectorXException {
        checkClosed();
        
        ByteBuffer encryptedBuffer = BufferUtils.createByteBuffer(encryptedVectors);
        int totalElements = numVectors * vectorSize;
        FloatBuffer vectorsBuffer = BufferUtils.createFloatBuffer(totalElements);
        
        int encryptedSizePerVector = encryptedVectors.length / numVectors;
        
        int result = nativeLib.vecx_decrypt_vectors(instance, encryptedBuffer, numVectors,
                                                   encryptedSizePerVector, vectorsBuffer, vectorSize);
        
        if (result != 0) {
            String error = getLastError();
            throw new VectorXException("Vectors decryption failed: " + error, result);
        }
        
        return BufferUtils.toVectorArray(vectorsBuffer, numVectors, vectorSize);
    }
    
    /**
     * Decrypts vectors and calculates similarities to a query vector in one operation.
     * 
     * @param encryptedVectors encrypted vectors data
     * @param numVectors number of vectors
     * @param vectorSize size of each vector
     * @param queryVector query vector to compare against
     * @return similarity scores for each vector
     * @throws VectorXException if operation fails
     */
    public float[] decryptAndCalculateSimilarities(byte[] encryptedVectors, int numVectors, 
                                                  int vectorSize, float[] queryVector) throws VectorXException {
        checkClosed();
        
        if (queryVector.length != vectorSize) {
            throw new IllegalArgumentException("Query vector size must match vector size");
        }
        
        ByteBuffer encryptedBuffer = BufferUtils.createByteBuffer(encryptedVectors);
        FloatBuffer queryBuffer = BufferUtils.createFloatBuffer(queryVector);
        FloatBuffer similaritiesBuffer = BufferUtils.createFloatBuffer(numVectors);
        
        int encryptedSizePerVector = encryptedVectors.length / numVectors;
        
        int result = nativeLib.vecx_decrypt_and_calculate_similarities(instance, encryptedBuffer,
                                                                      numVectors, encryptedSizePerVector,
                                                                      queryBuffer, vectorSize,
                                                                      similaritiesBuffer);
        
        if (result != 0) {
            String error = getLastError();
            throw new VectorXException("Decrypt and calculate similarities failed: " + error, result);
        }
        
        return BufferUtils.toFloatArray(similaritiesBuffer);
    }
    
    /**
     * Encrypts metadata using MessagePack serialization.
     * 
     * @param metadata the metadata object to encrypt
     * @return encrypted metadata
     * @throws VectorXException if encryption fails
     */
    public byte[] encryptMeta(Object metadata) throws VectorXException {
        checkClosed();
        
        // Serialize metadata to MessagePack format
        byte[] serialized = MessagePackUtils.packMetadata(metadata);
        
        ByteBuffer metadataBuffer = BufferUtils.createByteBuffer(serialized);
        int encryptedSize = BufferUtils.estimateEncryptedSize(serialized.length);
        ByteBuffer encryptedBuffer = BufferUtils.createByteBuffer(encryptedSize);
        
        int result = nativeLib.vecx_encrypt_meta(instance, metadataBuffer, serialized.length,
                                                encryptedBuffer, encryptedSize);
        
        if (result != 0) {
            String error = getLastError();
            throw new VectorXException("Metadata encryption failed: " + error, result);
        }
        
        // Return only the used portion
        byte[] encrypted = new byte[encryptedBuffer.position()];
        encryptedBuffer.flip();
        encryptedBuffer.get(encrypted);
        
        return encrypted;
    }
    
    /**
     * Decrypts metadata and deserializes from MessagePack format.
     * 
     * @param encryptedMeta encrypted metadata
     * @param valueType the class type to deserialize to
     * @param <T> the type parameter
     * @return decrypted and deserialized metadata
     * @throws VectorXException if decryption or deserialization fails
     */
    public <T> T decryptMeta(byte[] encryptedMeta, Class<T> valueType) throws VectorXException {
        checkClosed();
        
        ByteBuffer encryptedBuffer = BufferUtils.createByteBuffer(encryptedMeta);
        int metadataSize = encryptedMeta.length; // Estimate for decrypted size
        ByteBuffer metadataBuffer = BufferUtils.createByteBuffer(metadataSize);
        
        int result = nativeLib.vecx_decrypt_meta(instance, encryptedBuffer, encryptedMeta.length,
                                                metadataBuffer, metadataSize);
        
        if (result != 0) {
            String error = getLastError();
            throw new VectorXException("Metadata decryption failed: " + error, result);
        }
        
        // Extract decrypted data
        byte[] decrypted = new byte[metadataBuffer.position()];
        metadataBuffer.flip();
        metadataBuffer.get(decrypted);
        
        // Deserialize from MessagePack
        return MessagePackUtils.unpackMetadata(decrypted, valueType);
    }
    
    /**
     * Calculates distances between two sets of vectors.
     * 
     * @param vectors1 first set of vectors
     * @param vectors2 second set of vectors
     * @return distance matrix where element [i][j] is the distance between vectors1[i] and vectors2[j]
     * @throws VectorXException if calculation fails
     */
    public float[][] calculateDistances(float[][] vectors1, float[][] vectors2) throws VectorXException {
        checkClosed();
        
        if (vectors1.length == 0 || vectors2.length == 0) {
            throw new IllegalArgumentException("Vector sets cannot be empty");
        }
        
        int vectorSize = vectors1[0].length;
        if (vectors2[0].length != vectorSize) {
            throw new IllegalArgumentException("All vectors must have the same dimensions");
        }
        
        FloatBuffer buffer1 = BufferUtils.createFloatBufferFromVectors(vectors1);
        FloatBuffer buffer2 = BufferUtils.createFloatBufferFromVectors(vectors2);
        
        int numVectors1 = vectors1.length;
        int numVectors2 = vectors2.length;
        int totalDistances = numVectors1 * numVectors2;
        
        FloatBuffer distancesBuffer = BufferUtils.createFloatBuffer(totalDistances);
        
        int result = nativeLib.vecx_calculate_distances(instance, buffer1, buffer2,
                                                       numVectors1, numVectors2, vectorSize,
                                                       distancesBuffer);
        
        if (result != 0) {
            String error = getLastError();
            throw new VectorXException("Distance calculation failed: " + error, result);
        }
        
        // Convert flattened distances back to 2D array
        float[] flatDistances = BufferUtils.toFloatArray(distancesBuffer);
        float[][] distances = new float[numVectors1][numVectors2];
        
        for (int i = 0; i < numVectors1; i++) {
            System.arraycopy(flatDistances, i * numVectors2, distances[i], 0, numVectors2);
        }
        
        return distances;
    }
    
    /**
     * Gets the version of the native VectorX library.
     * 
     * @return version string
     */
    public String getVersion() {
        if (closed) {
            return "Unknown (instance closed)";
        }
        
        Pointer versionPtr = nativeLib.vecx_get_version();
        if (versionPtr != null) {
            return versionPtr.getString(0);
        }
        return "Unknown";
    }
    
    /**
     * Gets the last error message from the native library.
     * 
     * @return error message or null if no error
     */
    private String getLastError() {
        if (closed) {
            return "Instance is closed";
        }
        
        Pointer errorPtr = nativeLib.vecx_get_error(instance);
        if (errorPtr != null) {
            return errorPtr.getString(0);
        }
        return "Unknown error";
    }
    
    /**
     * Checks if the instance is closed and throws an exception if it is.
     * 
     * @throws VectorXException if the instance is closed
     */
    private void checkClosed() throws VectorXException {
        if (closed) {
            throw new VectorXException("VectorX instance has been closed");
        }
    }
    
    /**
     * Closes the VectorX instance and frees native resources.
     * This method is automatically called when using try-with-resources.
     */
    @Override
    public void close() {
        if (!closed && instance != null) {
            nativeLib.vecx_destroy(instance);
            closed = true;
            LOGGER.info("VectorX instance closed");
        }
    }
    
    /**
     * Ensures the native instance is properly cleaned up if close() was not called.
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
    
    /**
     * Checks if this instance is closed.
     * 
     * @return true if closed, false otherwise
     */
    public boolean isClosed() {
        return closed;
    }
}