package com.launchx.labs.vecx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic unit tests for VectorX Java client components.
 * Note: These tests do not require the native library to be present,
 * they test the Java-side logic and utilities.
 */
class VectorXTest {
    
    @Test
    @DisplayName("Buffer utilities should create and convert buffers correctly")
    void testBufferUtils() {
        // Test float array to buffer conversion
        float[] originalArray = {1.0f, 2.0f, 3.0f, 4.0f};
        var buffer = BufferUtils.createFloatBuffer(originalArray);
        
        assertNotNull(buffer);
        assertEquals(originalArray.length, buffer.capacity());
        
        // Test buffer to array conversion
        float[] convertedArray = BufferUtils.toFloatArray(buffer);
        assertArrayEquals(originalArray, convertedArray);
    }
    
    @Test
    @DisplayName("Buffer utilities should handle 2D vector arrays")
    void testVectorArrayConversion() {
        float[][] vectors = {
            {1.0f, 2.0f, 3.0f},
            {4.0f, 5.0f, 6.0f},
            {7.0f, 8.0f, 9.0f}
        };
        
        var buffer = BufferUtils.createFloatBufferFromVectors(vectors);
        assertNotNull(buffer);
        assertEquals(9, buffer.capacity()); // 3 vectors * 3 dimensions
        
        // Convert back to 2D array
        float[][] converted = BufferUtils.toVectorArray(buffer, 3, 3);
        assertEquals(vectors.length, converted.length);
        
        for (int i = 0; i < vectors.length; i++) {
            assertArrayEquals(vectors[i], converted[i]);
        }
    }
    
    @Test
    @DisplayName("MessagePack utilities should serialize and deserialize objects")
    void testMessagePackUtils() {
        // Test with a simple object
        TestMetadata original = new TestMetadata("test", 42, new String[]{"a", "b", "c"});
        
        assertDoesNotThrow(() -> {
            byte[] packed = MessagePackUtils.pack(original);
            assertNotNull(packed);
            assertTrue(packed.length > 0);
            
            TestMetadata unpacked = MessagePackUtils.unpack(packed, TestMetadata.class);
            assertEquals(original.name, unpacked.name);
            assertEquals(original.value, unpacked.value);
            assertArrayEquals(original.tags, unpacked.tags);
        });
    }
    
    @Test
    @DisplayName("VectorX exception should properly handle error codes")
    void testVectorXException() {
        VectorXException ex1 = new VectorXException("Test message");
        assertEquals(-1, ex1.getErrorCode());
        assertEquals("Test message", ex1.getMessage());
        
        VectorXException ex2 = new VectorXException("Test with code", 42);
        assertEquals(42, ex2.getErrorCode());
        assertTrue(ex2.toString().contains("Error Code: 42"));
    }
    
    @Test
    @DisplayName("Library loader should provide platform information")
    void testLibraryLoader() {
        String platformInfo = LibraryLoader.getPlatformInfo();
        assertNotNull(platformInfo);
        assertTrue(platformInfo.contains("OS:"));
        assertTrue(platformInfo.contains("Arch:"));
        assertTrue(platformInfo.contains("Library:"));
    }
    
    @Test
    @DisplayName("Buffer utilities should estimate encrypted size correctly")
    void testEncryptedSizeEstimation() {
        int originalSize = 100;
        int estimatedSize = BufferUtils.estimateEncryptedSize(originalSize);
        
        assertTrue(estimatedSize > originalSize);
        assertTrue(estimatedSize >= originalSize + 64); // Should add overhead
    }
    
    @Test
    @DisplayName("MessagePack vector data should serialize correctly")
    void testVectorDataSerialization() {
        float[][] vectors = {{1.0f, 2.0f}, {3.0f, 4.0f}};
        String metadata = "test metadata";
        
        assertDoesNotThrow(() -> {
            byte[] packed = MessagePackUtils.packVectorData(vectors, metadata);
            assertNotNull(packed);
            
            MessagePackUtils.VectorData unpacked = MessagePackUtils.unpackVectorData(packed);
            assertEquals(2, unpacked.getCount());
            assertEquals(2, unpacked.getDimensions());
            assertEquals(metadata, unpacked.getMetadata());
            
            float[][] unpackedVectors = unpacked.getVectors();
            assertEquals(vectors.length, unpackedVectors.length);
            for (int i = 0; i < vectors.length; i++) {
                assertArrayEquals(vectors[i], unpackedVectors[i]);
            }
        });
    }
    
    /**
     * Test metadata class for serialization tests.
     */
    public static class TestMetadata {
        public String name;
        public int value;
        public String[] tags;
        
        public TestMetadata() {} // Required for deserialization
        
        public TestMetadata(String name, int value, String[] tags) {
            this.name = name;
            this.value = value;
            this.tags = tags;
        }
    }
}