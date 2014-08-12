/*
 *
 * Date: 2011-feb-18
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.util;

import ngpoker.Version;
import net.sf.xapp.net.common.framework.StringSerializable;
import net.sf.xapp.net.server.util.filesystemstore.FileContent;
import net.sf.xapp.net.server.util.filesystemstore.RealFileSystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils
{
    public static void writeObjToFile(String version, String lispObj, String file)
    {
        String content = version + "\n" + lispObj;
        net.sf.xapp.utils.FileUtils.writeFile(content, file);
    }
    public static void writeObjToFile(StringSerializable obj, String file)
    {
        String content = Version.VERSION + "\n" + obj.serialize();
        net.sf.xapp.utils.FileUtils.writeFile(content, file);
    }
    public static void readObjFromFile(StringSerializable obj, String file)
    {
        FileContent fileContent = readFile(file);
        if(!fileContent.getVersion().equals(Version.VERSION))
        {
            throw new RuntimeException("bad file version: (required: " + Version.VERSION + ") " + fileContent);
        }
        obj.deserialize(fileContent.firstLine());
    }

    public static FileContent readFile(String filePath)
    {
        return RealFileSystem._readFile(filePath);
    }

    public static List<Byte> readFile(File file)
    {
        try
        {
            return readStream(new FileInputStream(file));
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
    public static List<Byte> readStream(InputStream in)
    {
        try
        {
            List<Byte> result = new ArrayList<Byte>();
            int b;
            while((b=in.read())!=-1)
            {
                result.add((byte)b);
            }
            in.close();
            return result;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    public static void writeFile(File file, List<Byte> data)
    {
        try
        {
            FileOutputStream out = new FileOutputStream(file);
            byte[] bytes = toByteArray(data);
            out.write(bytes);
            out.flush();
            out.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException();
        }
    }

    private static byte[] toByteArray(List<Byte> data)
    {
        byte[] bytes = new byte[data.size()];
        for (int i = 0; i < data.size(); i++)
        {
            Byte aByte = data.get(i);
            bytes[i] = aByte;
        }
        return bytes;
    }
}
