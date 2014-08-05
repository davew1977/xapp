/*
 *
 * Date: 2010-sep-16
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.connectionserver;

import net.sf.xapp.net.server.util.commandlineclient.CommandLine;

import java.io.*;
import java.net.Socket;

public class AdminClient
{
    public static void main(String[] args) throws IOException
    {
        final Socket s = new Socket("localhost", 1137);

        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String message;
                    while((message=br.readLine())!=null)
                    {
                        System.out.println(message);
                    }
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        System.out.println("connected to " + s);
        boolean alive = true;
        while (alive)
        {
            String command = CommandLine.readLine(System.in) + "\n";
            if (command.trim().equals("quit"))
            {
                System.exit(0);
            }
            else
            {
                s.getOutputStream().write(command.getBytes("UTF-8"));
                s.getOutputStream().flush();
            }
        }
    }
}
