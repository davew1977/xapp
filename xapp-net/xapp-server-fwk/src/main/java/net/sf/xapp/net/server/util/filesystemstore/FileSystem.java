/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.util.filesystemstore;

import java.io.IOException;
import java.util.List;

/**
 * Facade on the filesystem. Allows a "virtual" or mocked filesystem to be used at runtime
 */
public interface FileSystem
{
    void deleteAll();

    void createFile(String fileName, String header, String text);
    void createBinaryFile(String fileName, byte[] data, String suffix);
    void createRawFile(String fileName, String content);

    void deleteFile(String fileName);

    void append(String fileName, String line);

    /**
     *
     * @param filePath
     * @return lines
     */
    FileContent readFile(String filePath);

    /**
     *
     * @param fileName
     * @return null if key has no file
     */
    FileContent readFileFromKey(String fileName);
    String readRawFileFromKey(String fileName);

    /**
     *
     * @return
     */
    List<String> filePaths();

    void destroy() throws IOException;
}
