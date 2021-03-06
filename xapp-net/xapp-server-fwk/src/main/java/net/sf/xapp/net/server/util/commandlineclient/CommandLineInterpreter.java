/*
 *
 * Date: 2010-mar-08
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.util.commandlineclient;

import net.sf.xapp.net.server.clustering.PublicEntryPoint;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.TransportHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Utility to allow interacting with ngpoker node through System.in
 */
public class CommandLineInterpreter extends Thread
{
    private final boolean startThread;
    private boolean alive = true;
    private Logger log = LoggerFactory.getLogger(getClass());
    private final PublicEntryPoint publicEntryPoint;


    public CommandLineInterpreter(boolean startThread, PublicEntryPoint publicEntryPoint)
    {
        this.startThread = startThread;
        this.publicEntryPoint = publicEntryPoint;
    }

    @Override
    public void run()
    {
        log.info("Starting command line interpreter");
        while (alive)
        {
            try
            {
                String message = CommandLine.readLine(System.in);
                if (alive)
                {
                    InMessage m = TransportHelper.fromString(message);
                    publicEntryPoint.handleMessage(m);
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