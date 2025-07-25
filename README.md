# VectorX Java Client

[![Maven Central](https://img.shields.io/maven-central/v/com.launchx.labs/vecx-java.svg)](https://mvnrepository.com/artifact/com.launchx.labs/vecx-java)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://openjdk.java.net/)

Java client for VectorX - High-performance vector encryption and operations library.

## Overview

VectorX Java Client provides a complete Java interface to the VectorX C++ library, offering:

- **Vector Encryption/Decryption**: Secure encryption of individual vectors and batches
- **Metadata Support**: Encrypt and decrypt metadata using MessagePack serialization
- **Similarity Search**: Efficient similarity calculations on encrypted vectors
- **Distance Calculations**: Compute distances between vector sets
- **Cross-Platform**: Supports Windows, macOS, and Linux (x86_64, ARM64)
- **Memory Efficient**: Uses direct buffers for optimal native interop performance

## Features

- **FFI Integration**: Uses JNA (Java Native Access) for seamless C++ library integration
- **Dynamic Library Loading**: Automatic platform detection and library loading
- **Buffer Management**: Efficient direct buffer handling for large vector operations  
- **MessagePack Serialization**: Built-in support for metadata serialization/deserialization
- **Resource Management**: Automatic memory cleanup with try-with-resources support
- **Error Handling**: Comprehensive exception handling with native error codes

## Installation

### Maven

```xml
<dependency>
    <groupId>com.launchx.labs</groupId>
    <artifactId>vecx-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.launchx.labs:vecx-java:1.0.0-SNAPSHOT'
```

### Requirements

- Java 11 or higher
- VectorX native library (automatically loaded if included in resources)

## Quick Start

```java
import com.launchx.labs.vecx.LibVectorX;
import com.launchx.labs.vecx.VectorXException;
import java.util.Arrays;

public class QuickStart {
    public static void main(String[] args) throws VectorXException {
        // Create VectorX instance (auto-closeable)
        try (LibVectorX vectorX = new LibVectorX()) {
            
            // Encrypt a vector
            float[] vector = {1.0f, 2.0f, 3.0f, 4.0f};
            byte[] encrypted = vectorX.encryptVector(vector);
            
            // Decrypt the vector
            float[] decrypted = vectorX.decryptVector(encrypted, vector.length);
            
            System.out.println("Original:  " + Arrays.toString(vector));
            System.out.println("Decrypted: " + Arrays.toString(decrypted));
        }
    }
}
```

## API Reference

### Core Classes

#### `LibVectorX`
Main client class for VectorX operations.

```java
// Create instance
LibVectorX vectorX = new LibVectorX();

// Vector operations
byte[] encrypted = vectorX.encryptVector(float[] vector);
float[] decrypted = vectorX.decryptVector(byte[] encrypted, int size);

// Batch operations  
byte[] encryptedBatch = vectorX.encryptVectors(float[][] vectors);
float[][] decryptedBatch = vectorX.decryptVectors(byte[] encrypted, int numVectors, int vectorSize);

// Metadata operations
byte[] encryptedMeta = vectorX.encryptMeta(Object metadata);
<T> T decryptedMeta = vectorX.decryptMeta(byte[] encrypted, Class<T> type);

// Similarity search
float[] similarities = vectorX.decryptAndCalculateSimilarities(
    byte[] encryptedVectors, int numVectors, int vectorSize, float[] queryVector);

// Distance calculation
float[][] distances = vectorX.calculateDistances(float[][] vectors1, float[][] vectors2);

// Cleanup
vectorX.close(); // Or use try-with-resources
```

#### `BufferUtils`
Utilities for efficient buffer management.

```java
// Create buffers
FloatBuffer floatBuf = BufferUtils.createFloatBuffer(float[] data);
ByteBuffer byteBuf = BufferUtils.createByteBuffer(byte[] data);

// Convert vectors
FloatBuffer vectorsBuf = BufferUtils.createFloatBufferFromVectors(float[][] vectors);
float[][] vectors = BufferUtils.toVectorArray(FloatBuffer buffer, int numVectors, int vectorSize);
```

#### `MessagePackUtils`
MessagePack serialization utilities.

```java
// Serialize/deserialize
byte[] packed = MessagePackUtils.pack(Object obj);
<T> T unpacked = MessagePackUtils.unpack(byte[] data, Class<T> type);

// Vector data with metadata
byte[] vectorData = MessagePackUtils.packVectorData(float[][] vectors, Object metadata);
VectorData data = MessagePackUtils.unpackVectorData(byte[] packed);
```

### Exception Handling

```java
try (LibVectorX vectorX = new LibVectorX()) {
    // VectorX operations
} catch (VectorXException e) {
    System.err.println("Error: " + e.getMessage());
    System.err.println("Error Code: " + e.getErrorCode());
}
```

## Examples

### Basic Vector Encryption

```java
try (LibVectorX vectorX = new LibVectorX()) {
    float[] vector = {1.5f, 2.0f, 3.5f, 4.0f, 5.5f};
    
    // Encrypt
    byte[] encrypted = vectorX.encryptVector(vector);
    System.out.println("Encrypted size: " + encrypted.length + " bytes");
    
    // Decrypt
    float[] decrypted = vectorX.decryptVector(encrypted, vector.length);
    System.out.println("Vectors match: " + Arrays.equals(vector, decrypted));
}
```

### Metadata Encryption with Custom Objects

```java
public class DocumentMetadata {
    public String id;
    public String category;
    public long timestamp;
    public int dimensions;
    
    // constructors, getters, setters...
}

try (LibVectorX vectorX = new LibVectorX()) {
    DocumentMetadata metadata = new DocumentMetadata("doc_001", "text", System.currentTimeMillis(), 128);
    
    // Encrypt metadata
    byte[] encrypted = vectorX.encryptMeta(metadata);
    
    // Decrypt metadata
    DocumentMetadata decrypted = vectorX.decryptMeta(encrypted, DocumentMetadata.class);
}
```

### Batch Vector Processing

```java
try (LibVectorX vectorX = new LibVectorX()) {
    float[][] vectors = {
        {1.0f, 2.0f, 3.0f},
        {4.0f, 5.0f, 6.0f},
        {7.0f, 8.0f, 9.0f}
    };
    
    // Encrypt all vectors at once
    byte[] encrypted = vectorX.encryptVectors(vectors);
    
    // Decrypt all vectors at once
    float[][] decrypted = vectorX.decryptVectors(encrypted, vectors.length, vectors[0].length);
}
```

### Similarity Search

```java
try (LibVectorX vectorX = new LibVectorX()) {
    // Database vectors
    float[][] database = {
        {1.0f, 0.0f, 0.0f},
        {0.0f, 1.0f, 0.0f},
        {0.0f, 0.0f, 1.0f}
    };
    
    // Query vector
    float[] query = {0.9f, 0.1f, 0.0f};
    
    // Encrypt database
    byte[] encryptedDB = vectorX.encryptVectors(database);
    
    // Search (decrypt + calculate similarities in one operation)
    float[] similarities = vectorX.decryptAndCalculateSimilarities(
        encryptedDB, database.length, database[0].length, query);
    
    // Find best match
    int bestMatch = 0;
    for (int i = 1; i < similarities.length; i++) {
        if (similarities[i] > similarities[bestMatch]) {
            bestMatch = i;
        }
    }
    System.out.println("Best match: index " + bestMatch + " (similarity: " + similarities[bestMatch] + ")");
}
```

### Distance Matrix Calculation

```java
try (LibVectorX vectorX = new LibVectorX()) {
    float[][] set1 = {{1.0f, 0.0f}, {0.0f, 1.0f}};
    float[][] set2 = {{1.0f, 1.0f}, {2.0f, 0.0f}};
    
    float[][] distances = vectorX.calculateDistances(set1, set2);
    
    // distances[i][j] = distance between set1[i] and set2[j]
    for (int i = 0; i < distances.length; i++) {
        for (int j = 0; j < distances[i].length; j++) {
            System.out.printf("Distance[%d][%d] = %.4f\n", i, j, distances[i][j]);
        }
    }
}
```

## Platform Support

The Java client automatically detects the platform and loads the appropriate native library:

- **Windows**: `vectorx-x64.dll`, `vectorx-x86.dll`  
- **macOS**: `libvectorx-x64.dylib`, `libvectorx-arm64.dylib`
- **Linux**: `libvectorx-x64.so`, `libvectorx-arm64.so`

Libraries should be placed in the `src/main/resources/native/` directory or available in the system library path.

## Building from Source

```bash
git clone https://github.com/LaunchX-Labs/vecx-java.git
cd vecx-java
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Running Examples

```bash
mvn exec:java -Dexec.mainClass="com.launchx.labs.vecx.examples.BasicUsageExample"
```

## Performance Considerations

- Use direct buffers for large vector operations
- Prefer batch operations (`encryptVectors`/`decryptVectors`) for multiple vectors
- Use `decryptAndCalculateSimilarities` instead of separate decrypt + similarity operations
- Reuse `LibVectorX` instances when possible (they are thread-safe)
- Use try-with-resources to ensure proper cleanup

## Error Handling

All VectorX operations can throw `VectorXException` with detailed error information:

```java
try {
    vectorX.encryptVector(vector);
} catch (VectorXException e) {
    int errorCode = e.getErrorCode();  // Native error code
    String message = e.getMessage();   // Error description
    Throwable cause = e.getCause();    // Underlying cause (if any)
}
```

Common error scenarios:
- Native library not found or incompatible
- Invalid vector dimensions
- Encryption/decryption failures
- Memory allocation failures

## Thread Safety

`LibVectorX` instances are thread-safe and can be used concurrently from multiple threads. However, each instance manages native resources, so proper cleanup is important.

## Memory Management

The Java client automatically manages native memory through:
- JNA automatic memory mapping
- Direct buffer allocation/deallocation  
- Native instance lifecycle management via `close()`
- Finalizer as backup for cleanup

Always use try-with-resources or explicitly call `close()`:

```java
// Preferred: try-with-resources
try (LibVectorX vectorX = new LibVectorX()) {
    // operations
} // automatically closed

// Alternative: explicit cleanup
LibVectorX vectorX = new LibVectorX();
try {
    // operations
} finally {
    vectorX.close();
}
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)  
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

For questions, bug reports, or feature requests:
- Open an issue on GitHub
- Contact: [support@launchx-labs.com](mailto:support@launchx-labs.com)

## Changelog

### 1.0.0-SNAPSHOT
- Initial release
- Complete JNA bindings for VectorX C++ library
- MessagePack serialization support
- Cross-platform native library loading
- Comprehensive test suite and examples
