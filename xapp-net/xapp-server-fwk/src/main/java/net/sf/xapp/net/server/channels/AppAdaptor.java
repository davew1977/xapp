package net.sf.xapp.net.server.channels;


import net.sf.xapp.net.common.types.AppType;
import net.sf.xapp.net.common.types.UserId;

public abstract class AppAdaptor implements App
{
    @Override
    public void userConnected(UserId userId) {

    }

    @Override
    public void userDisconnected(UserId userId) {

    }

    @Override
    public void userJoined(UserId userId) {

    }

    @Override
    public void userLeft(UserId userId) {

    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public void setCommChannel(CommChannel channel) {

    }

    @Override
    public AppType getAppType() {
        return null;
    }
}
