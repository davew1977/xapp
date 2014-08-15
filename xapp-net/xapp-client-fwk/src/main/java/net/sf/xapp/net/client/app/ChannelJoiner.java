package net.sf.xapp.net.client.app;

import net.sf.xapp.net.common.types.AppType;

public interface ChannelJoiner
{
    void joinChannel(String id, AppType appType);
    void joinChannel(String id, AppType appType, Object attachment);
    void channelLeft(String id, AppType appType);
}
