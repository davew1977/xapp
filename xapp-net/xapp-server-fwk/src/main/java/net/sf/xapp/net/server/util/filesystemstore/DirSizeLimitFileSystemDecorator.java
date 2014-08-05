/*
 *
 * Date: 2010-sep-13
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.util.filesystemstore;

import java.io.IOException;
import java.util.List;

public class DirSizeLimitFileSystemDecorator implements FileSystem
{
    private final FileSystem delegate;

    public DirSizeLimitFileSystemDecorator(FileSystem delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void deleteAll()
    {
        delegate.deleteAll();
    }

    @Override
    public void createFile(String fileName, String header, String text)
    {
        delegate.createFile(keyToFilename(fileName), header, text);
    }

    @Override
    public void createBinaryFile(String fileName, byte[] data, String suffix)
    {
        delegate.createBinaryFile(keyToFilename(fileName), data, suffix);
    }

    @Override
    public void createRawFile(String fileName, String content)
    {
        delegate.createRawFile(keyToFilename(fileName), content);
    }

    @Override
    public String readRawFileFromKey(String fileName)
    {
        return delegate.readRawFileFromKey(keyToFilename(fileName));
    }

    @Override
    public void deleteFile(String fileName)
    {
        delegate.deleteFile(keyToFilename(fileName));
    }

    @Override
    public void append(String fileName, String line)
    {
        delegate.append(keyToFilename(fileName), line);
    }

    @Override
    public FileContent readFile(String filePath)
    {
        return delegate.readFile(filePath);
    }

    @Override
    public FileContent readFileFromKey(String fileName)
    {
        return delegate.readFile(keyToFilename(fileName));
    }

    @Override
    public List<String> filePaths()
    {
        return delegate.filePaths();
    }

    @Override
    public void destroy() throws IOException
    {
        delegate.destroy();
    }

    /**
     * because of a 1024 file count in a directory on the live FS
     * we have to ensure that we don't exceed that number of files per dir.
     *
     * This algorithm will return a file path based on the input string. Examples:
     * T-1-1000  => T-/1-/10/T-1-1000
     * T-1-10001 => T-/1-/10/00/T-1-10001
     * hello     => he/ll/hello
     * me        => me
     * foo       => fo/foo
     * @param key
     * @return
     */
    static String keyToFilename(String key)
    {
        String filepath = "";
        String keyCopy = key;
        while(key.length()>2)
        {
            String nextChunk = key.substring(0,2);
            key = key.substring(2);
            filepath += nextChunk + "/";
        }
        return filepath + keyCopy;
    }
}
