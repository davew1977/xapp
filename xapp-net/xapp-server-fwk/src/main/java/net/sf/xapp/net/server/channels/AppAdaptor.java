package net.sf.xapp.net.server.channels;

import ngpoker.common.types.PlayerId;

public abstract class AppAdaptor implements App
{
    @Override
    public void playerConnected(PlayerId playerId)
    {

    }

    @Override
    public void playerDisconnected(PlayerId playerId)
    {

    }

    @Override
    public void playerJoined(PlayerId playerId)
    {

    }

    @Override
    public void playerLeft(PlayerId playerId)
    {

    }

    @Override
    public void setCommChannel(CommChannel channel)
    {

    }
}
