package com.launchx.labs.vecx.examples;

import com.launchx.labs.vecx.LibVectorX;
import com.launchx.labs.vecx.VectorXException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic usage examples for VectorX Java client.
 * 
 * This class demonstrates common operations including vector encryption/decryption,
 * metadata handling, and batch operations.
 * 
 * Note: These examples require the VectorX native library to be available.
 */
public class BasicUsageExample {
    
    public static void main(String[] args) {
        try {
            basicVectorOperations();
            metadataOperations();
            batchOperations();
            similaritySearch();
            distanceCalculation();
        } catch (Exception e) {
            System.err.println("Example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Demonstrates basic vector encryption and decryption.
     */
    public static void basicVectorOperations() throws VectorXException {
        System.out.println("=== Basic Vector Operations ===");
        
        try (LibVectorX vectorX = new LibVectorX()) {
            System.out.println("VectorX Version: " + vectorX.getVersion());
            
            // Create a sample vector
            float[] originalVector = {1.5f, 2.0f, 3.5f, 4.0f, 5.5f};
            System.out.println("Original vector: " + Arrays.toString(originalVector));
            
            // Encrypt the vector
            byte[] encryptedVector = vectorX.encryptVector(originalVector);
            System.out.println("Encrypted vector size: " + encryptedVector.length + " bytes");
            
            // Decrypt the vector
            float[] decryptedVector = vectorX.decryptVector(encryptedVector, originalVector.length);
            System.out.println("Decrypted vector: " + Arrays.toString(decryptedVector));
            
            // Verify the vectors match
            boolean matches = Arrays.equals(originalVector, decryptedVector);
            System.out.println("Vectors match: " + matches);
            
            System.out.println();
        }
    }
    
    /**
     * Demonstrates metadata encryption and decryption with MessagePack.
     */
    public static void metadataOperations() throws VectorXException {
        System.out.println("=== Metadata Operations ===");
        
        try (LibVectorX vectorX = new LibVectorX()) {
            // Create sample metadata
            Map<String, Object> originalMetadata = new HashMap<>();
            originalMetadata.put("id", "vector_001");
            originalMetadata.put("category", "text_embedding");
            originalMetadata.put("timestamp", System.currentTimeMillis());
            originalMetadata.put("dimensions", 128);
            
            System.out.println("Original metadata: " + originalMetadata);
            
            // Encrypt the metadata
            byte[] encryptedMetadata = vectorX.encryptMeta(originalMetadata);
            System.out.println("Encrypted metadata size: " + encryptedMetadata.length + " bytes");
            
            // Decrypt the metadata
            @SuppressWarnings("unchecked")
            Map<String, Object> decryptedMetadata = vectorX.decryptMeta(encryptedMetadata, Map.class);
            System.out.println("Decrypted metadata: " + decryptedMetadata);
            
            // Verify metadata matches
            boolean matches = originalMetadata.equals(decryptedMetadata);
            System.out.println("Metadata matches: " + matches);
            
            System.out.println();
        }
    }
    
    /**
     * Demonstrates batch vector operations.
     */
    public static void batchOperations() throws VectorXException {
        System.out.println("=== Batch Operations ===");
        
        try (LibVectorX vectorX = new LibVectorX()) {
            // Create multiple vectors
            float[][] originalVectors = {
                {1.0f, 2.0f, 3.0f},
                {4.0f, 5.0f, 6.0f},
                {7.0f, 8.0f, 9.0f},
                {10.0f, 11.0f, 12.0f}
            };
            
            System.out.println("Original vectors:");
            for (int i = 0; i < originalVectors.length; i++) {
                System.out.println("  Vector " + i + ": " + Arrays.toString(originalVectors[i]));
            }
            
            // Encrypt all vectors in batch
            byte[] encryptedVectors = vectorX.encryptVectors(originalVectors);
            System.out.println("Encrypted vectors size: " + encryptedVectors.length + " bytes");
            
            // Decrypt all vectors in batch
            float[][] decryptedVectors = vectorX.decryptVectors(
                encryptedVectors, 
                originalVectors.length, 
                originalVectors[0].length
            );
            
            System.out.println("Decrypted vectors:");
            for (int i = 0; i < decryptedVectors.length; i++) {
                System.out.println("  Vector " + i + ": " + Arrays.toString(decryptedVectors[i]));
            }
            
            // Verify vectors match
            boolean allMatch = true;
            for (int i = 0; i < originalVectors.length; i++) {
                if (!Arrays.equals(originalVectors[i], decryptedVectors[i])) {
                    allMatch = false;
                    break;
                }
            }
            System.out.println("All vectors match: " + allMatch);
            
            System.out.println();
        }
    }
    
    /**
     * Demonstrates similarity search with encrypted vectors.
     */
    public static void similaritySearch() throws VectorXException {
        System.out.println("=== Similarity Search ===");
        
        try (LibVectorX vectorX = new LibVectorX()) {
            // Create database vectors
            float[][] databaseVectors = {
                {1.0f, 0.0f, 0.0f},  // Similar to query
                {0.0f, 1.0f, 0.0f},  // Orthogonal to query
                {0.0f, 0.0f, 1.0f},  // Orthogonal to query
                {0.7f, 0.7f, 0.0f}   // Somewhat similar to query
            };
            
            // Query vector
            float[] queryVector = {1.0f, 0.1f, 0.0f};
            
            System.out.println("Query vector: " + Arrays.toString(queryVector));
            System.out.println("Database vectors:");
            for (int i = 0; i < databaseVectors.length; i++) {
                System.out.println("  Vector " + i + ": " + Arrays.toString(databaseVectors[i]));
            }
            
            // Encrypt database vectors
            byte[] encryptedDatabase = vectorX.encryptVectors(databaseVectors);
            
            // Perform similarity search (decrypt and calculate similarities in one operation)
            float[] similarities = vectorX.decryptAndCalculateSimilarities(
                encryptedDatabase,
                databaseVectors.length,
                databaseVectors[0].length,
                queryVector
            );
            
            System.out.println("Similarity scores:");
            for (int i = 0; i < similarities.length; i++) {
                System.out.printf("  Vector %d: %.4f\n", i, similarities[i]);
            }
            
            // Find most similar vector
            int mostSimilarIndex = 0;
            for (int i = 1; i < similarities.length; i++) {
                if (similarities[i] > similarities[mostSimilarIndex]) {
                    mostSimilarIndex = i;
                }
            }
            
            System.out.println("Most similar vector: Index " + mostSimilarIndex + 
                             " with similarity " + similarities[mostSimilarIndex]);
            
            System.out.println();
        }
    }
    
    /**
     * Demonstrates distance calculation between vector sets.
     */
    public static void distanceCalculation() throws VectorXException {
        System.out.println("=== Distance Calculation ===");
        
        try (LibVectorX vectorX = new LibVectorX()) {
            // Create two sets of vectors
            float[][] vectorSet1 = {
                {1.0f, 0.0f},
                {0.0f, 1.0f}
            };
            
            float[][] vectorSet2 = {
                {1.0f, 1.0f},
                {2.0f, 0.0f},
                {0.0f, 2.0f}
            };
            
            System.out.println("Vector Set 1:");
            for (int i = 0; i < vectorSet1.length; i++) {
                System.out.println("  " + Arrays.toString(vectorSet1[i]));
            }
            
            System.out.println("Vector Set 2:");
            for (int i = 0; i < vectorSet2.length; i++) {
                System.out.println("  " + Arrays.toString(vectorSet2[i]));
            }
            
            // Calculate distances between all pairs
            float[][] distances = vectorX.calculateDistances(vectorSet1, vectorSet2);
            
            System.out.println("Distance matrix:");
            for (int i = 0; i < distances.length; i++) {
                System.out.print("  Row " + i + ": ");
                for (int j = 0; j < distances[i].length; j++) {
                    System.out.printf("%.4f ", distances[i][j]);
                }
                System.out.println();
            }
            
            System.out.println();
        }
    }
}