package ai.vectorx;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Utils {
  public static byte[] jsonZip(Map<String, Object> map) throws IOException {
    if (map == null || map.isEmpty()) {
      return new byte[0];
    }

    if (map == null || map.isEmpty()) {
      return new byte[0]; // same as b'' in Python
    }

    // 1. Convert map to JSON
    ObjectMapper jsonMapper = new ObjectMapper();
    String jsonString = jsonMapper.writeValueAsString(map);

    // 2. Convert JSON string to UTF-8 bytes
    byte[] input = jsonString.getBytes("UTF-8");

    // 3. Compress using zlib (Deflater)
    Deflater deflater = new Deflater();
    deflater.setInput(input);
    deflater.finish();

    byte[] buffer = new byte[1024];
    int compressedDataLength = deflater.deflate(buffer);
    deflater.end();

    // 4. Copy only the compressed part
    byte[] output = new byte[compressedDataLength];
    System.arraycopy(buffer, 0, output, 0, compressedDataLength);
//    System.out.println("Output" + output);

    return output;
  }

  public static Map<String, Object> jsonUnzip(byte[] compressedData) throws IOException {
    if (compressedData == null || compressedData.length == 0) {
      return Map.of(); // same as {} in Python
    }

    Inflater inflater = new Inflater();
    inflater.setInput(compressedData);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];

    try {
      while (!inflater.finished()) {
        int count = inflater.inflate(buffer);
        if (count == 0 && inflater.needsInput()) {
          break; // avoid infinite loop if data is malformed
        }
        baos.write(buffer, 0, count);
      }
    } catch (Exception e) {
      throw new IOException("Failed to decompress JSON", e);
    } finally {
      inflater.end();
    }

    // 1. Convert decompressed bytes back to String
    String jsonString = baos.toString("UTF-8");

    // 2. Parse JSON string into Map<String, Object>
    ObjectMapper jsonMapper = new ObjectMapper();
    return jsonMapper.readValue(jsonString, Map.class);
  }

}
