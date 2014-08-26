/*
 *
 * Date: 2010-mar-08
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.util.commandlineclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Utility to allow interacting with ngpoker node through System.in
 */
public class CommandLineUtil extends Thread
{
    private final boolean startThread;
    private boolean alive = true;
    private Logger log = LoggerFactory.getLogger(getClass());
    private final CommandLineListener listener;


    public CommandLineUtil(boolean startThread, CommandLineListener listener)
    {
        this.startThread = startThread;
        this.listener = listener;
    }

    @Override
    public void run()
    {
        log.info("Starting command line interpreter, listener: " + listener);
        while (alive)
        {
            try
            {
                String message = CommandLine.readLine(System.in);
                if (alive)
                {
                    listener.handleCommand(message);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @PostConstruct
    public void init()
    {
        if (startThread)
        {
            start();
        }
        else
        {
            log.info("Command Line Interpreter NOT started");
        }
    }

    @PreDestroy
    public void shutdown()
    {
        alive = false;
    }
}