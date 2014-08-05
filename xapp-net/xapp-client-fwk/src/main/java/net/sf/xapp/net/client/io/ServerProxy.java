/*
 *
 * Date: 2010-okt-01
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.io;

import ngpoker.common.framework.MessageHandler;

public interface ServerProxy extends MessageHandler, Connectable
{
    void addListener(ConnectionListener client);

    void setClient(MessageHandler messageHandler);
}
