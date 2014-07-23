/*
 *
 * Date: 2011-jan-08
 * Author: davidw
 *
 */
package net.sf.xapp.net.connectionserver.mina;

import net.sf.xapp.Global;
import net.sf.xapp.net.common.framework.TransportObject;
import ngpoker.common.types.MessageTypeEnum;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;

public class BytePacketDecoder extends CumulativeProtocolDecoder
{
    private final int maxMessageLength;

    public BytePacketDecoder(int maxMessageLength)
    {
        this.maxMessageLength = maxMessageLength;
    }

    @Override
    protected boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception
    {
        if (in.prefixedDataAvailable(4, maxMessageLength))
        {
            int length = in.getInt();
            int messageType = in.getInt();
            TransportObject message = Global.create(messageType);
            byte[] bytes = new byte[length - 4];
            in.get(bytes);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            DataInput din = new DataInputStream(bais);
            message.readData(din);
            out.write(message);
            return true;
        }
        else
        {
            return false;
        }
    }
}
