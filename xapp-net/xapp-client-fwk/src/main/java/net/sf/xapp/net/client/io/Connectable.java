package net.sf.xapp.net.client.io;

import net.sf.xapp.net.client.framework.Callback;

public interface Connectable
{
    boolean connect(Callback onConnect);

    void disconnect();

    boolean isConnected();
}
