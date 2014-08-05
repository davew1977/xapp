package net.sf.xapp.net.server.channels;

import ngpoker.common.types.*;


/**
 * Generated 2011-sep-27 08:27:04
 * 
 */
public interface App
{

     void playerConnected(PlayerId playerId);
     void playerDisconnected(PlayerId playerId);
     void playerJoined(PlayerId playerId);
     void playerLeft(PlayerId playerId);

    String getKey();

    void setCommChannel(CommChannel channel);

    AppType getAppType();
}
