/*
 *
 * Date: 2011-jan-10
 * Author: davidw
 *
 */
package net.sf.xapp.net.connectionserver.mina;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class BytePacketCodecFactory implements ProtocolCodecFactory
{
    private static final int MAX_FROM_CLIENT = 8182;
    private static final int MAX_FROM_SERVER = Integer.MAX_VALUE;

    private final BytePacketDecoder decoder;
    private final BytePacketEncoder encoder;

    public BytePacketCodecFactory(boolean isServer)
    {
        decoder = new BytePacketDecoder(isServer ? MAX_FROM_CLIENT : MAX_FROM_SERVER);
        encoder = new BytePacketEncoder(isServer ? MAX_FROM_SERVER : MAX_FROM_CLIENT);
    }

    @Override
    public ProtocolEncoder getEncoder() throws Exception
    {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder() throws Exception
    {
        return decoder;
    }
}
