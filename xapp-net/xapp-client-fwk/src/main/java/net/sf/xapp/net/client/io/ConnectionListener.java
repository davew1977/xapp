/*
 *
 * Date: 2010-sep-17
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.io;

/**
 * Something that connects to a server
 */
public interface ConnectionListener
{
    void disconnected();

    void connected();

    void handleConnectException(Exception e);
}
