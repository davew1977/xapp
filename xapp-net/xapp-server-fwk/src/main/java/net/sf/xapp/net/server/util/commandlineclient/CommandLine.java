/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.util.commandlineclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Simple utility for reading input from System.in
 */
public class CommandLine
{
    public static String readLine()
    {
        return readLine(System.in);
    }
    public static String readLine(InputStream in)
    {
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);
        String s = null;
        try
        {
            return br.readLine();
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
    }
}