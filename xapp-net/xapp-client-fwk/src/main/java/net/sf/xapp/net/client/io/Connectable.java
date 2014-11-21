package net.sf.xapp.net.client.io;

import net.sf.xapp.net.client.framework.Callback;

public interface Connectable
{
    /**
     * application level request to go online
     */
    boolean connect();

    void setConnecting();

    /**
     * application level request to go offline
     */
    void setOffline();

    boolean isConnected();
}
