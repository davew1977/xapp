/*
 *
 * Date: 2010-sep-17
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.connectionserver;

import net.sf.xapp.net.common.framework.Message;

public interface IOLayer<T,E>
{
    void sendMessage(T session, Message message);
    void closeSession(T session);

    E getSessionKey(T session);
    void setSessionKey(T session, E sessionKey);
}
