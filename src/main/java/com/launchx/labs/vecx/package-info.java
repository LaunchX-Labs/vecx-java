/**
 * VectorX Java Client - High-performance vector encryption and operations.
 * 
 * <p>This package provides a complete Java interface to the VectorX C++ library,
 * offering secure vector operations including encryption, decryption, similarity 
 * calculations, and distance computations.
 * 
 * <h2>Core Components:</h2>
 * <ul>
 *   <li>{@link com.launchx.labs.vecx.LibVectorX} - Main client class for VectorX operations</li>
 *   <li>{@link com.launchx.labs.vecx.VectorXNative} - JNA interface to native library</li>
 *   <li>{@link com.launchx.labs.vecx.BufferUtils} - Utilities for efficient buffer management</li>
 *   <li>{@link com.launchx.labs.vecx.MessagePackUtils} - MessagePack serialization utilities</li>
 *   <li>{@link com.launchx.labs.vecx.LibraryLoader} - Cross-platform native library loading</li>
 *   <li>{@link com.launchx.labs.vecx.VectorXException} - Exception handling for VectorX operations</li>
 * </ul>
 * 
 * <h2>Quick Start:</h2>
 * <pre>{@code
 * try (LibVectorX vectorX = new LibVectorX()) {
 *     float[] vector = {1.0f, 2.0f, 3.0f, 4.0f};
 *     byte[] encrypted = vectorX.encryptVector(vector);
 *     float[] decrypted = vectorX.decryptVector(encrypted, vector.length);
 * }
 * }</pre>
 * 
 * <h2>Features:</h2>
 * <ul>
 *   <li>Vector encryption and decryption (single and batch)</li>
 *   <li>Metadata encryption with MessagePack serialization</li>
 *   <li>Similarity search on encrypted vectors</li>
 *   <li>Distance calculation between vector sets</li>
 *   <li>Cross-platform native library support</li>
 *   <li>Memory-efficient direct buffer operations</li>
 *   <li>Automatic resource management</li>
 * </ul>
 * 
 * <h2>Platform Support:</h2>
 * <p>Supports Windows, macOS, and Linux on x86_64 and ARM64 architectures.
 * Native libraries are automatically detected and loaded based on the current platform.</p>
 * 
 * <h2>Thread Safety:</h2>
 * <p>The main {@link com.launchx.labs.vecx.LibVectorX} class is thread-safe and can be used
 * concurrently from multiple threads.</p>
 * 
 * <h2>Memory Management:</h2>
 * <p>Use try-with-resources or explicitly call {@link com.launchx.labs.vecx.LibVectorX#close()}
 * to ensure proper cleanup of native resources.</p>
 * 
 * @author LaunchX Labs
 * @version 1.0.0-SNAPSHOT
 * @since 1.0.0
 */
package com.launchx.labs.vecx;