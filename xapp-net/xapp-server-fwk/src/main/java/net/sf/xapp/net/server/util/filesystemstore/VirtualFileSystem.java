/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.util.filesystemstore;

import java.io.IOException;
import java.util.*;

/**
 * Encapsulate in-memory filesystem. Useful for testing
 */
public class VirtualFileSystem implements FileSystem
{
    private Map<String, StringBuilder> m_fileMap;

    public VirtualFileSystem()
    {
        m_fileMap = new TreeMap<String, StringBuilder>();
    }

    public VirtualFileSystem(String data)
    {
        this();
        if(data!=null)
        {
            load(data);
        }
    }

    @Override
    public void destroy() throws IOException
    {

    }

    public void load(String data)
    {
        String[] lines = data.split("\n");
        for (String line : lines)
        {
            String[] args = line.split("=");
            append(args[0], args[1]);
        }
    }


    public String save()
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, StringBuilder> entry : m_fileMap.entrySet())
        {
            String[] lines = entry.getValue().toString().split("\n");
            for (String line : lines)
            {
                sb.append(entry.getKey()).append("=").append(line).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public FileContent readFileFromKey(String fileName)
    {
        return readFile(fileName);
    }

    public void createFile(String fileName, String header, String text)
    {
        m_fileMap.put(fileName, new StringBuilder(header).append("\n").append(text).append("\n"));
    }

    @Override
    public void createBinaryFile(String fileName, byte[] data, String suffix)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createRawFile(String fileName, String content)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readRawFileFromKey(String fileName)
    {
        throw new UnsupportedOperationException();
    }

    public void deleteAll()
    {
        for (String s : filePaths())
        {
            deleteFile(s);
        }
    }

    public void deleteFile(String fileName)
    {
        m_fileMap.remove(fileName);
    }

    public void append(String fileName, String line)
    {
        StringBuilder sb = get(fileName);
        if(sb==null)
        {
            sb = new StringBuilder();
            m_fileMap.put(fileName, sb);
        }
        sb.append(line).append("\n");
    }

    private StringBuilder get(String filename)
    {
        return m_fileMap.get(filename);
    }

    public FileContent readFile(String filePath)
    {
        String[] linesArray = get(filePath).toString().split("\n");
        List<String> lines = Arrays.asList(linesArray);
        return new FileContent(null, lines.get(0), lines.subList(1, lines.size()));
    }

    public List<String> filePaths()
    {
        return new ArrayList<String>(m_fileMap.keySet());
    }

    public Map<String, StringBuilder> getFileMap()
    {
        return m_fileMap;
    }

    public String toString()
    {
        return save();
    }
}
