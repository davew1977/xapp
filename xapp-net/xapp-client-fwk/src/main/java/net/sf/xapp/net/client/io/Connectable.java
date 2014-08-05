package net.sf.xapp.net.client.io;

public interface Connectable
{
    boolean connect();

    void disconnect();

    boolean isConnected();
}
