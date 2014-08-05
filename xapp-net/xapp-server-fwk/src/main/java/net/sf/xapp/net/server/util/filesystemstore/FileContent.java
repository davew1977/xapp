/*
 *
 * Date: 2010-mar-12
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.util.filesystemstore;

import java.io.File;
import java.util.List;

public class FileContent
{
    private final String rootType;
    private final File m_srcFile;
    private final List<String> m_lines;
    private final String m_version;
    private final long baseSeqNo;


    public FileContent(File srcFile, String version, long baseSeqNo, String rootType, List<String> lines)
    {
        this(srcFile, version + "," + rootType + "," + baseSeqNo, lines);
    }
    public FileContent(File srcFile, String firstLine, List<String> lines)
    {
        m_lines = lines;
        m_srcFile = srcFile;

        if(firstLine.contains(","))
        {
            String[] args = firstLine.split(",");
            m_version = args[0];
            rootType = args[1];
            baseSeqNo = args.length > 2 ? Long.parseLong(args[2]) : 0;
        }
        else
        {
            m_version = firstLine;
            rootType = null;
            baseSeqNo = 0;
        }

    }

    public List<String> getLines()
    {
        return m_lines;
    }

    public String getVersion()
    {
        return m_version;
    }

    public File getSrcFile()
    {
        return m_srcFile;
    }

    public String toFileString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(m_version).append('\n');
        for (String line : m_lines)
        {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "FileContent{" +
                "m_srcFile=" + m_srcFile +
                ", m_lines=" + m_lines +
                ", m_version='" + m_version + '\'' +
                '}';
    }

    public String lineAt(int i)
    {
        return getLines().get(i);
    }

    public String firstLine()
    {
        return lineAt(0);
    }

    public List<String> linesAfterFirst()
    {
        return getLines().subList(1, getLines().size());
    }

    public String getRootType()
    {
        return rootType;
    }

    public long getBaseSeqNo() {
        return baseSeqNo;
    }
}
