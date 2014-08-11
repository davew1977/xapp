package net.sf.xapp.net.server.channels;


import net.sf.xapp.net.common.types.AppType;
import net.sf.xapp.net.common.types.UserId;

/**
 * Generated 2011-sep-27 08:27:04
 * 
 */
public interface App
{

     void userConnected(UserId userId);
     void userDisconnected(UserId userId);
     void userJoined(UserId userId);
     void userLeft(UserId userId);

    String getKey();

    void setCommChannel(CommChannel channel);

    AppType getAppType();
}
