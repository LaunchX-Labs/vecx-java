# Native Libraries Directory

This directory should contain the platform-specific VectorX native libraries:

## File naming convention:

### Windows:
- `vectorx-x64.dll` (64-bit Intel/AMD)
- `vectorx-x86.dll` (32-bit Intel/AMD)

### macOS:
- `libvectorx-x64.dylib` (64-bit Intel)
- `libvectorx-arm64.dylib` (64-bit Apple Silicon)

### Linux:
- `libvectorx-x64.so` (64-bit Intel/AMD)
- `libvectorx-arm64.so` (64-bit ARM)

## Usage:

The VectorX Java client will automatically detect the current platform and attempt to load the appropriate library from this directory. If no library is found here, it will fall back to searching the system library path.

To include native libraries in your application:
1. Place the appropriate library files in this directory
2. Build your project with Maven/Gradle
3. The libraries will be included in the resulting JAR file
4. At runtime, they will be extracted to a temporary location and loaded

## Building Native Libraries:

The native libraries should be built from the VectorX C++ source code. Refer to the VectorX C++ documentation for build instructions.