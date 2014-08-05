/*
 *
 * Date: 2010-mar-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.io;

public class HostInfo implements Cloneable
{
    public String host;
    public int port;

    public HostInfo()
    {
    }

    public HostInfo(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString()
    {
        return host + ":" + port;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public static HostInfo parse(String arg)
    {
        HostInfo n = new HostInfo();
        String[] chunks = arg.split(":");
        /*
       allows list of host:port OR ports only
        */
        n.port = Integer.parseInt(chunks[chunks.length == 1 ? 0 : 1]);
        n.host = chunks.length == 1 ? "localhost" : chunks[0];
        return n;
    }
}