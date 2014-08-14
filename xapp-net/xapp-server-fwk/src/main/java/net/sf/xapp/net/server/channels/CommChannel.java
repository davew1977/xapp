/*
 *
 * Date: 2010-sep-12
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import net.sf.xapp.net.common.framework.Message;
import net.sf.xapp.net.common.types.UserId;

public interface CommChannel
{
    void broadcast(Message message);
    void send(UserId userId, Message message);
    void removeUser(UserId userId);
    void addUser(UserId userId);
}
