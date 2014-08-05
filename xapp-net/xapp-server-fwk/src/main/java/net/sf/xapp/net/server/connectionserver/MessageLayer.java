/*
 *
 * Date: 2010-sep-17
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.connectionserver;

import ngpoker.common.framework.InMessage;

public interface MessageLayer<T, E>
{
    void setIOLayer(IOLayer<T, E> ioLayer);
    void sessionOpened(T session);
    void sessionClosed(T session);
    void handleMessage(T session, InMessage message);
    
}
