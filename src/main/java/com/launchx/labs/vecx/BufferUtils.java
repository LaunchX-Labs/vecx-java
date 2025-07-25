package com.launchx.labs.vecx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Utility class for managing direct buffers used in native operations.
 * Provides convenience methods for creating and manipulating FloatBuffer and ByteBuffer
 * instances for efficient data transfer with native code.
 */
public class BufferUtils {
    
    /**
     * Create a direct FloatBuffer from a float array.
     * 
     * @param data The float array to wrap
     * @return A direct FloatBuffer containing the data
     */
    public static FloatBuffer createFloatBuffer(float[] data) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(data.length * Float.BYTES)
                                     .order(ByteOrder.nativeOrder())
                                     .asFloatBuffer();
        buffer.put(data);
        buffer.flip();
        return buffer;
    }
    
    /**
     * Create a direct FloatBuffer with the specified capacity.
     * 
     * @param capacity The number of floats the buffer should hold
     * @return A direct FloatBuffer with the specified capacity
     */
    public static FloatBuffer createFloatBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity * Float.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
    }
    
    /**
     * Create a direct ByteBuffer from a byte array.
     * 
     * @param data The byte array to wrap
     * @return A direct ByteBuffer containing the data
     */
    public static ByteBuffer createByteBuffer(byte[] data) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length)
                                    .order(ByteOrder.nativeOrder());
        buffer.put(data);
        buffer.flip();
        return buffer;
    }
    
    /**
     * Create a direct ByteBuffer with the specified capacity.
     * 
     * @param capacity The number of bytes the buffer should hold
     * @return A direct ByteBuffer with the specified capacity
     */
    public static ByteBuffer createByteBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity)
                        .order(ByteOrder.nativeOrder());
    }
    
    /**
     * Convert a FloatBuffer to a float array.
     * 
     * @param buffer The FloatBuffer to convert
     * @return A float array containing the buffer data
     */
    public static float[] toFloatArray(FloatBuffer buffer) {
        // Save current position
        int position = buffer.position();
        buffer.rewind();
        
        float[] array = new float[buffer.remaining()];
        buffer.get(array);
        
        // Restore position
        buffer.position(position);
        return array;
    }
    
    /**
     * Convert a ByteBuffer to a byte array.
     * 
     * @param buffer The ByteBuffer to convert
     * @return A byte array containing the buffer data
     */
    public static byte[] toByteArray(ByteBuffer buffer) {
        // Save current position
        int position = buffer.position();
        buffer.rewind();
        
        byte[] array = new byte[buffer.remaining()];
        buffer.get(array);
        
        // Restore position
        buffer.position(position);
        return array;
    }
    
    /**
     * Create a FloatBuffer from a 2D float array (vectors).
     * The data is stored in row-major order (flattened).
     * 
     * @param vectors 2D array where each row is a vector
     * @return A direct FloatBuffer containing all vectors flattened
     */
    public static FloatBuffer createFloatBufferFromVectors(float[][] vectors) {
        if (vectors == null || vectors.length == 0) {
            return createFloatBuffer(0);
        }
        
        int numVectors = vectors.length;
        int vectorSize = vectors[0].length;
        int totalElements = numVectors * vectorSize;
        
        FloatBuffer buffer = createFloatBuffer(totalElements);
        
        for (float[] vector : vectors) {
            if (vector.length != vectorSize) {
                throw new IllegalArgumentException("All vectors must have the same size");
            }
            buffer.put(vector);
        }
        
        buffer.flip();
        return buffer;
    }
    
    /**
     * Convert a flattened FloatBuffer back to a 2D float array.
     * 
     * @param buffer The FloatBuffer containing flattened vector data
     * @param numVectors Number of vectors in the buffer
     * @param vectorSize Size of each vector
     * @return 2D float array where each row is a vector
     */
    public static float[][] toVectorArray(FloatBuffer buffer, int numVectors, int vectorSize) {
        if (buffer.remaining() < numVectors * vectorSize) {
            throw new IllegalArgumentException("Buffer does not contain enough data for the specified dimensions");
        }
        
        // Save current position
        int position = buffer.position();
        buffer.rewind();
        
        float[][] vectors = new float[numVectors][vectorSize];
        
        for (int i = 0; i < numVectors; i++) {
            buffer.get(vectors[i]);
        }
        
        // Restore position
        buffer.position(position);
        return vectors;
    }
    
    /**
     * Calculate the required buffer size for encrypted data.
     * This is an estimation based on typical encryption overhead.
     * 
     * @param originalSize The size of the original data
     * @return Estimated size needed for encrypted data
     */
    public static int estimateEncryptedSize(int originalSize) {
        // Add padding and encryption overhead (typically 16-32 bytes for AES)
        return originalSize + 64; // Conservative estimate
    }
    
    /**
     * Clear a buffer by setting all bytes to zero.
     * 
     * @param buffer The buffer to clear
     */
    public static void clearBuffer(ByteBuffer buffer) {
        int position = buffer.position();
        buffer.rewind();
        
        while (buffer.hasRemaining()) {
            buffer.put((byte) 0);
        }
        
        buffer.position(position);
    }
    
    /**
     * Clear a float buffer by setting all values to zero.
     * 
     * @param buffer The buffer to clear
     */
    public static void clearBuffer(FloatBuffer buffer) {
        int position = buffer.position();
        buffer.rewind();
        
        while (buffer.hasRemaining()) {
            buffer.put(0.0f);
        }
        
        buffer.position(position);
    }
}