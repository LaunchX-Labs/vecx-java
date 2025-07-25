package com.launchx.labs.vecx;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * JNA interface for the VectorX native library.
 * This interface provides bindings to all native methods available in the C++ VectorX library.
 */
public interface VectorXNative extends Library {
    
    /**
     * Create a new VectorX instance.
     * 
     * @return Pointer to the VectorX instance, or null on failure
     */
    Pointer vecx_create();
    
    /**
     * Destroy a VectorX instance and free associated memory.
     * 
     * @param instance Pointer to the VectorX instance
     */
    void vecx_destroy(Pointer instance);
    
    /**
     * Encrypt a single vector.
     * 
     * @param instance Pointer to the VectorX instance
     * @param vector Input vector data
     * @param vectorSize Size of the input vector
     * @param encryptedVector Output buffer for encrypted vector
     * @param encryptedSize Size of the output buffer
     * @return 0 on success, negative error code on failure
     */
    int vecx_encrypt_vector(Pointer instance, FloatBuffer vector, int vectorSize, 
                           ByteBuffer encryptedVector, int encryptedSize);
    
    /**
     * Decrypt a single vector.
     * 
     * @param instance Pointer to the VectorX instance
     * @param encryptedVector Input encrypted vector data
     * @param encryptedSize Size of the encrypted vector
     * @param vector Output buffer for decrypted vector
     * @param vectorSize Size of the output buffer
     * @return 0 on success, negative error code on failure
     */
    int vecx_decrypt_vector(Pointer instance, ByteBuffer encryptedVector, int encryptedSize,
                           FloatBuffer vector, int vectorSize);
    
    /**
     * Encrypt multiple vectors in batch.
     * 
     * @param instance Pointer to the VectorX instance
     * @param vectors Input vectors data (flattened)
     * @param numVectors Number of vectors
     * @param vectorSize Size of each vector
     * @param encryptedVectors Output buffer for encrypted vectors
     * @param encryptedSize Total size of output buffer
     * @return 0 on success, negative error code on failure
     */
    int vecx_encrypt_vectors(Pointer instance, FloatBuffer vectors, int numVectors, int vectorSize,
                            ByteBuffer encryptedVectors, int encryptedSize);
    
    /**
     * Decrypt multiple vectors in batch.
     * 
     * @param instance Pointer to the VectorX instance
     * @param encryptedVectors Input encrypted vectors data
     * @param numVectors Number of vectors
     * @param encryptedSize Size of encrypted data per vector
     * @param vectors Output buffer for decrypted vectors
     * @param vectorSize Size of each output vector
     * @return 0 on success, negative error code on failure
     */
    int vecx_decrypt_vectors(Pointer instance, ByteBuffer encryptedVectors, int numVectors, 
                            int encryptedSize, FloatBuffer vectors, int vectorSize);
    
    /**
     * Decrypt vectors and calculate similarities in one operation.
     * 
     * @param instance Pointer to the VectorX instance
     * @param encryptedVectors Input encrypted vectors
     * @param numVectors Number of vectors
     * @param encryptedSize Size of encrypted data per vector
     * @param queryVector Query vector to compare against
     * @param querySize Size of query vector
     * @param similarities Output buffer for similarity scores
     * @return 0 on success, negative error code on failure
     */
    int vecx_decrypt_and_calculate_similarities(Pointer instance, ByteBuffer encryptedVectors, 
                                              int numVectors, int encryptedSize,
                                              FloatBuffer queryVector, int querySize,
                                              FloatBuffer similarities);
    
    /**
     * Encrypt metadata.
     * 
     * @param instance Pointer to the VectorX instance
     * @param metadata Input metadata buffer
     * @param metadataSize Size of input metadata
     * @param encryptedMeta Output buffer for encrypted metadata
     * @param encryptedSize Size of output buffer
     * @return 0 on success, negative error code on failure
     */
    int vecx_encrypt_meta(Pointer instance, ByteBuffer metadata, int metadataSize,
                         ByteBuffer encryptedMeta, int encryptedSize);
    
    /**
     * Decrypt metadata.
     * 
     * @param instance Pointer to the VectorX instance
     * @param encryptedMeta Input encrypted metadata
     * @param encryptedSize Size of encrypted metadata
     * @param metadata Output buffer for decrypted metadata
     * @param metadataSize Size of output buffer
     * @return 0 on success, negative error code on failure
     */
    int vecx_decrypt_meta(Pointer instance, ByteBuffer encryptedMeta, int encryptedSize,
                         ByteBuffer metadata, int metadataSize);
    
    /**
     * Calculate distances between vectors.
     * 
     * @param instance Pointer to the VectorX instance
     * @param vectors1 First set of vectors
     * @param vectors2 Second set of vectors
     * @param numVectors1 Number of vectors in first set
     * @param numVectors2 Number of vectors in second set
     * @param vectorSize Size of each vector
     * @param distances Output buffer for distance matrix
     * @return 0 on success, negative error code on failure
     */
    int vecx_calculate_distances(Pointer instance, FloatBuffer vectors1, FloatBuffer vectors2,
                                int numVectors1, int numVectors2, int vectorSize,
                                FloatBuffer distances);
    
    /**
     * Get the last error message from the native library.
     * 
     * @param instance Pointer to the VectorX instance
     * @return Pointer to error message string, or null if no error
     */
    Pointer vecx_get_error(Pointer instance);
    
    /**
     * Get version information of the native library.
     * 
     * @return Pointer to version string
     */
    Pointer vecx_get_version();
}