/*
 *
 * Date: 2010-sep-12
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.channels;

import ngpoker.common.framework.Message;
import ngpoker.common.types.PlayerId;

public interface CommChannel
{
    void broadcast(Message message);
    void send(PlayerId playerId, Message message);
    void removePlayer(PlayerId playerId);
    void addPlayer(PlayerId playerId);
}
