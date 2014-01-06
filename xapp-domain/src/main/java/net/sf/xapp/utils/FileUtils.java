/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 * The contents of this file may be used under the terms of the GNU Lesser
 * General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package net.sf.xapp.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.net.URL;
import java.net.MalformedURLException;

public class FileUtils
{
    public void writeFile(String content, File file)
    {
        _writeFile(content, file);
    }

    public static void _writeFile(String content, File file)
    {
        writeFile(content, file, null);
    }

    public static void writeFile(String content, File file, String encoding)
    {

        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = encoding != null ? new OutputStreamWriter(fos, encoding) : new OutputStreamWriter(fos);
            osw.write(content);
            osw.flush();
            osw.close();
        }
        catch (IOException e)
        {
            throw new XappException(e);
        }
    }

    public static void writeFile(String content, String name)
    {
        writeFile(content, name, null);
    }

    public static void writeFile(String content, String name, String encoding)
    {
        writeFile(content, new File(name), encoding);
    }

    public static void writeProperties(Properties props, File file)
    {
        try
        {
            props.store(new FileOutputStream(file), "");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static String readFile(File file)
    {
        return readFile(file, null);
    }

    public static String readFile(File file, Charset charset)
    {
        try
        {
            return readInputToString(new FileInputStream(file), charset);
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Properties readProperties(String file) throws FileNotFoundException
    {
        return readProperties(new File(file));
    }

    public static Properties readProperties(File file) throws FileNotFoundException
    {
        Properties props = new Properties();
        try
        {
            props.load(new FileInputStream(file));
        }
        catch (FileNotFoundException fnfe)
        {
            throw fnfe;
        }
        catch (IOException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return props;
    }

    public static String readInputToString(InputStream is)
    {
        return readInputToString(is, null);
    }

    public static String readURL(String url)
    {
        return readInputToString(FileUtils.class.getResourceAsStream(url));
    }

    public static String readInputToString(InputStream is, Charset charset)
    {
        InputStreamReader reader = charset != null ? new InputStreamReader(is, charset) : new InputStreamReader(is);
        BufferedReader br = new BufferedReader(reader);
        String line;
        StringBuilder sb = new StringBuilder();
        try
        {
            while ((line = br.readLine()) != null)
            {
                sb.append(line).append('\n');
            }
            br.close();
        }
        catch (IOException e)
        {
            throw new XappException(e);
        }
        return sb.toString();
    }

    /**
     * Read the inputstream to the file supplied.
     *
     * @param is   The read from stream
     * @param file The read to file
     * @throws IOException, the inputstream will be closed.
     */
    public static void readInputToFile(InputStream is, File file) throws IOException
    {
        OutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int len;

            while ((len = is.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
        }
        finally
        {
            out.close();
            is.close();
        }
    }

    public static boolean fileAccessedSince(File file, long time)
    {
        return false;
    }

    public static void main(String[] args)
    {
        System.out.println("fg");
        boolean b = new File("C:\\dev\\svn-stuff\\branding\\brands\\master_config\\master_config_1\\assets\\fonts\\Calibri_bold.ttf").setLastModified(1231919744000L);
        System.out.println(b);
    }


    public static int exec(String command, String workingDir)
    {
        try
        {
            Process process = Runtime.getRuntime().exec(command, null, workingDir != null ? new File(workingDir) : null);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String brline;
            while ((brline = br.readLine()) != null)
            {
                System.out.println(brline);
            }
            br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((brline = br.readLine()) != null)
            {
                System.out.println(brline);
            }
            process.waitFor();
            return process.exitValue();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static boolean downloadFile(File target, String urlStr)
    {
        InputStream in = openStream(urlStr);
        if (in != null)
        {
            System.out.println("downloading " + urlStr + "...");
            writeStreamToFile(target, in);
            System.out.println("   ...done");
        }
        else
        {
            System.out.println("no file found at " + urlStr);
        }
        return in != null;
    }

    public static String downloadToString(String url)
    {
        InputStream inputStream = openStream(url);
        if(inputStream==null)
        {
            return null;
        }
        return new String(streamToByteArray(inputStream), Charset.forName("UTF-8"));
    }

    /**
     * First reads in all bytes fromt the stream into a byte buffer and then flushes them to file
     *
     * @param target
     * @param in
     */
    private static void writeStreamToFile(File target, InputStream in)
    {
        try
        {
            byte[] byteArray = streamToByteArray(in);
            FileOutputStream fileOutputStream = new FileOutputStream(target);
            fileOutputStream.write(byteArray);
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static byte[] streamToByteArray(InputStream in)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int i;
            while ((i = in.read()) != -1)
            {
                baos.write(i);
            }
            byte[] byteArray = baos.toByteArray();
            return byteArray;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


    public static InputStream openStream(String urlStr)
    {
        URL url;
        try
        {
            url = new URL(urlStr);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
        InputStream in;
        try
        {
            in = url.openStream();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return in;
    }

    /**
     * recursively lists the files in a directory whose name matches the regexp.
     *
     * regexp is not matched against directory names
     *
     * @param dir
     * @param regexp
     * @return list of file paths relative to the given directory
     */
    public static List<String> listRecursive(File dir, String regexp)
    {
        return listRecursive(dir, "", regexp);
    }
    private static List<String> listRecursive(File dir, String path, String regexp)
    {
        File[] files = dir.listFiles();
        List<String> filePaths = new ArrayList<String>();
        for (File file : files)
        {
            String filename = path + file.getName();
            if(file.isDirectory())
            {
                filePaths.addAll(listRecursive(file, filename + "/", regexp));
            }
            else
            {
                if (regexp==null || file.getName().matches(regexp))
                {
                    filePaths.add(filename);
                }
            }
        }
        return filePaths;
    }
}
