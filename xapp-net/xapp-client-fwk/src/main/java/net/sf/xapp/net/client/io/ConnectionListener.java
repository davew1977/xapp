/*
 *
 * Date: 2010-sep-17
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.io;

import net.sf.xapp.net.common.types.ConnectionState;

/**
 * Something that connects to a server
 */
public interface ConnectionListener
{
    void connectionStateChanged(ConnectionState newState);

    void handleConnectException(Exception e);
}
