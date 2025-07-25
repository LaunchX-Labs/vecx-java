package com.launchx.labs.vecx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Utility class for MessagePack serialization and deserialization.
 * Provides convenient methods for converting Java objects to/from MessagePack binary format,
 * replicating the functionality of the Python client's msgpack usage.
 */
public class MessagePackUtils {
    
    private static final ObjectMapper messagePackMapper = new ObjectMapper(new MessagePackFactory());
    
    /**
     * Serialize an object to MessagePack binary format.
     * 
     * @param object The object to serialize
     * @return Byte array containing the MessagePack data
     * @throws VectorXException if serialization fails
     */
    public static byte[] pack(Object object) throws VectorXException {
        try {
            return messagePackMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new VectorXException("Failed to serialize object to MessagePack", e);
        }
    }
    
    /**
     * Deserialize MessagePack binary data to an object of the specified type.
     * 
     * @param data The MessagePack binary data
     * @param valueType The class type to deserialize to
     * @param <T> The type parameter
     * @return The deserialized object
     * @throws VectorXException if deserialization fails
     */
    public static <T> T unpack(byte[] data, Class<T> valueType) throws VectorXException {
        try {
            return messagePackMapper.readValue(data, valueType);
        } catch (IOException e) {
            throw new VectorXException("Failed to deserialize MessagePack data", e);
        }
    }
    
    /**
     * Deserialize MessagePack binary data to a Map.
     * This is useful for dynamic data structures.
     * 
     * @param data The MessagePack binary data
     * @return A Map containing the deserialized data
     * @throws VectorXException if deserialization fails
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> unpackToMap(byte[] data) throws VectorXException {
        try {
            return messagePackMapper.readValue(data, Map.class);
        } catch (IOException e) {
            throw new VectorXException("Failed to deserialize MessagePack data to Map", e);
        }
    }
    
    /**
     * Serialize metadata object to MessagePack format for encryption.
     * 
     * @param metadata The metadata object to serialize
     * @return Byte array suitable for encryption
     * @throws VectorXException if serialization fails
     */
    public static byte[] packMetadata(Object metadata) throws VectorXException {
        return pack(metadata);
    }
    
    /**
     * Deserialize metadata from MessagePack format after decryption.
     * 
     * @param data The decrypted MessagePack data
     * @param valueType The expected type of the metadata
     * @param <T> The type parameter
     * @return The deserialized metadata object
     * @throws VectorXException if deserialization fails
     */
    public static <T> T unpackMetadata(byte[] data, Class<T> valueType) throws VectorXException {
        return unpack(data, valueType);
    }
    
    /**
     * Serialize vector metadata with additional context information.
     * 
     * @param vectors The vector data
     * @param metadata Additional metadata
     * @return Serialized data containing both vectors and metadata
     * @throws VectorXException if serialization fails
     */
    public static byte[] packVectorData(float[][] vectors, Object metadata) throws VectorXException {
        VectorData vectorData = new VectorData();
        vectorData.vectors = vectors;
        vectorData.metadata = metadata;
        vectorData.dimensions = vectors.length > 0 ? vectors[0].length : 0;
        vectorData.count = vectors.length;
        
        return pack(vectorData);
    }
    
    /**
     * Deserialize vector data with metadata.
     * 
     * @param data The serialized vector data
     * @return VectorData object containing vectors and metadata
     * @throws VectorXException if deserialization fails
     */
    public static VectorData unpackVectorData(byte[] data) throws VectorXException {
        return unpack(data, VectorData.class);
    }
    
    /**
     * Data class for storing vectors with associated metadata.
     */
    public static class VectorData {
        public float[][] vectors;
        public Object metadata;
        public int dimensions;
        public int count;
        
        public VectorData() {}
        
        public VectorData(float[][] vectors, Object metadata) {
            this.vectors = vectors;
            this.metadata = metadata;
            this.dimensions = vectors.length > 0 ? vectors[0].length : 0;
            this.count = vectors.length;
        }
        
        public float[][] getVectors() {
            return vectors;
        }
        
        public void setVectors(float[][] vectors) {
            this.vectors = vectors;
            this.dimensions = vectors.length > 0 ? vectors[0].length : 0;
            this.count = vectors.length;
        }
        
        public Object getMetadata() {
            return metadata;
        }
        
        public void setMetadata(Object metadata) {
            this.metadata = metadata;
        }
        
        public int getDimensions() {
            return dimensions;
        }
        
        public void setDimensions(int dimensions) {
            this.dimensions = dimensions;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
    }
    
    /**
     * Get the ObjectMapper instance used for MessagePack operations.
     * This allows for custom configuration if needed.
     * 
     * @return The MessagePack ObjectMapper instance
     */
    public static ObjectMapper getMapper() {
        return messagePackMapper;
    }
}