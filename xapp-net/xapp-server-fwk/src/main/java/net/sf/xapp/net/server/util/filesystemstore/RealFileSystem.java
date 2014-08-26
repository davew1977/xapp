/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.util.filesystemstore;

import net.sf.xapp.utils.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link FileSystem} that interacts with actual OS
 */
public class RealFileSystem implements FileSystem
{
    private String m_path;
    private Map<String, Writer> m_writers;
    private AntFacade m_antFacade;
    private final Logger m_log = LoggerFactory.getLogger(getClass());

    /**
     * @param peristentNodeId the node id for the backup store
     * @param partitionName   the special directory for this "Filesytem" under which files can be stored
     */
    public RealFileSystem(String backupDir, String peristentNodeId, String partitionName)
    {
        this(backupDir + "/" + peristentNodeId + "/" + partitionName);
    }

    public RealFileSystem(String path)
    {
        this.m_path = path;
        m_log.info("backup dir is " + m_path);
        new File(m_path).mkdirs();
        m_writers = new ConcurrentHashMap<String, Writer>();
        m_antFacade = new AntFacade();
    }

    public void deleteAll()
    {
        /*
        first close all writers
         */
        for (String filename : filePaths())
        {
            closeWriter(filename);
        }
        /*
        delete entire directory
         */
        Delete del = new Delete();
        del.setProject(new Project());
        File rootDir = new File(m_path);
        del.setDir(rootDir);
        del.execute();

        rootDir.mkdirs();
    }

    public void createFile(String fileName, String header, String text)
    {
        createRawFile(fileName, header + "\n" + text);
    }

    @Override
    public void createRawFile(String fileName, String content)
    {
        closeWriter(fileName);
        write(content, createWriter(fileName, false));
    }

    @Override
    public void createBinaryFile(String fileName, byte[] data, String suffix)
    {
        File file = new File(fileName(fileName + "." + suffix));
        file.getParentFile().mkdirs();
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            out.write(data);
            out.flush();
            out.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException();
        }
    }

    public void deleteFile(String fileName)
    {
        closeWriter(fileName);
        File file = new File(fileName(fileName));
        if (file.exists())
        {
            m_antFacade.deleteFile(file);
        }
    }

    private void closeWriter(String name)
    {
        try
        {
            getAppendWriter(name).close();
            m_writers.remove(name);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String fileName(String name)
    {
        return m_path + "/" + name;
    }

    private Writer getAppendWriter(String filename)
    {
        Writer writer = m_writers.get(filename);
        if (writer == null)
        {
            writer = createWriter(filename, true);
            m_writers.put(filename, writer);
        }
        return writer;
    }

    private OutputStreamWriter createWriter(String filename, boolean append)
    {
        try
        {
            File file = new File(fileName(filename));
            file.getParentFile().mkdirs();
            return new OutputStreamWriter(new FileOutputStream(file, append));
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void append(String fileName, String line)
    {
        Writer writer = getAppendWriter(fileName);
        write(line, writer);
    }

    private void write(String line, Writer writer)
    {
        try
        {
            writer.write(line + "\n");
            writer.flush();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public FileContent readFile(String filePath)
    {
        return _readFile(fileName(filePath));
    }

    @Override
    public FileContent readFileFromKey(String fileName)
    {
        try
        {
            return readFile(fileName(fileName));
        }
        catch (RuntimeException e)
        {
            if (e.getCause() instanceof FileNotFoundException)
            {
                return null;
            }
            else
            {
                throw e;
            }
        }
    }

    @Override
    public String readRawFileFromKey(String fileName)
    {
        File file = new File(fileName(fileName));
        if(!file.exists())
        {
            return null;
        }
        return FileUtils.readFile(file);
    }

    public static FileContent _readFile(String filePath)
    {
        File file = new File(filePath);
        String s = FileUtils.readFile(file);
        List<String> lines = Arrays.asList(s.split("\n"));
        return new FileContent(file, lines.get(0), lines.subList(1, lines.size()));
    }

    /**
     * has to walk the entire file tree
     *
     * @return
     */
    public List<String> filePaths()
    {
        File dir = new File(m_path);
        if (!dir.exists())
        {
            throw new RuntimeException("back up dir does not exist: " + m_path);
        }
        if (!dir.isDirectory())
        {
            throw new RuntimeException("back up dir is not a directory: " + m_path);
        }
        return FileUtils.listRecursive(dir, null);
    }

    @Override
    public void destroy() throws IOException
    {
        for (Writer writer : m_writers.values())
        {
            writer.close();
        }
        m_writers.clear();
    }
}
