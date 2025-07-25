package com.launchx.labs.vecx;

import com.sun.jna.Native;
import com.sun.jna.Platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

/**
 * Utility class for loading the VectorX native library across different platforms.
 * Handles dynamic loading of .so (Linux), .dll (Windows), and .dylib (macOS) files.
 */
public class LibraryLoader {
    private static final Logger LOGGER = Logger.getLogger(LibraryLoader.class.getName());
    
    private static final String LIBRARY_NAME = "vectorx";
    private static VectorXNative nativeInstance;
    private static boolean loaded = false;
    
    /**
     * Get the platform-specific library filename.
     * 
     * @return The library filename for the current platform
     */
    private static String getLibraryFileName() {
        String arch = System.getProperty("os.arch");
        String os = System.getProperty("os.name").toLowerCase();
        
        // Normalize architecture names
        if (arch.equals("amd64") || arch.equals("x86_64")) {
            arch = "x64";
        } else if (arch.equals("x86")) {
            arch = "x86";
        } else if (arch.startsWith("arm") || arch.startsWith("aarch")) {
            arch = "arm64";
        }
        
        // Determine OS-specific library extension and prefix
        if (os.contains("win")) {
            return String.format("%s-%s.dll", LIBRARY_NAME, arch);
        } else if (os.contains("mac") || os.contains("darwin")) {
            return String.format("lib%s-%s.dylib", LIBRARY_NAME, arch);
        } else {
            // Assume Linux/Unix
            return String.format("lib%s-%s.so", LIBRARY_NAME, arch);
        }
    }
    
    /**
     * Extract the native library from resources to a temporary file.
     * 
     * @param libraryFileName The name of the library file to extract
     * @return Path to the extracted temporary file
     * @throws IOException if extraction fails
     */
    private static Path extractLibraryFromResources(String libraryFileName) throws IOException {
        String resourcePath = "/native/" + libraryFileName;
        
        try (InputStream inputStream = LibraryLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Native library not found in resources: " + resourcePath);
            }
            
            // Create temporary file
            String tempFileName = "vecx_" + System.currentTimeMillis() + "_" + libraryFileName;
            Path tempFile = Files.createTempFile(tempFileName, null);
            
            // Extract library to temporary file
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            // Make it executable on Unix systems
            if (!Platform.isWindows()) {
                tempFile.toFile().setExecutable(true);
            }
            
            // Mark for deletion on exit
            tempFile.toFile().deleteOnExit();
            
            LOGGER.info("Extracted native library to: " + tempFile.toString());
            return tempFile;
        }
    }
    
    /**
     * Load the VectorX native library.
     * First attempts to load from system library path, then from resources.
     * 
     * @return The loaded VectorXNative instance
     * @throws UnsatisfiedLinkError if the library cannot be loaded
     */
    public static synchronized VectorXNative loadLibrary() {
        if (loaded && nativeInstance != null) {
            return nativeInstance;
        }
        
        String libraryFileName = getLibraryFileName();
        LOGGER.info("Attempting to load native library: " + libraryFileName);
        
        // First try to load from system library path
        try {
            nativeInstance = Native.load(LIBRARY_NAME, VectorXNative.class);
            loaded = true;
            LOGGER.info("Successfully loaded native library from system path");
            return nativeInstance;
        } catch (UnsatisfiedLinkError e) {
            LOGGER.warning("Failed to load from system path: " + e.getMessage());
        }
        
        // Try to load from resources
        try {
            Path tempLibraryPath = extractLibraryFromResources(libraryFileName);
            nativeInstance = Native.load(tempLibraryPath.toString(), VectorXNative.class);
            loaded = true;
            LOGGER.info("Successfully loaded native library from resources");
            return nativeInstance;
        } catch (IOException e) {
            LOGGER.severe("Failed to extract library from resources: " + e.getMessage());
            throw new UnsatisfiedLinkError("Could not extract native library: " + e.getMessage());
        } catch (UnsatisfiedLinkError e) {
            LOGGER.severe("Failed to load extracted library: " + e.getMessage());
            throw new UnsatisfiedLinkError("Could not load native library: " + e.getMessage());
        }
    }
    
    /**
     * Check if the native library is loaded.
     * 
     * @return true if loaded, false otherwise
     */
    public static boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Get the current OS and architecture information.
     * 
     * @return String describing the current platform
     */
    public static String getPlatformInfo() {
        return String.format("OS: %s, Arch: %s, Library: %s", 
                           System.getProperty("os.name"),
                           System.getProperty("os.arch"),
                           getLibraryFileName());
    }
}