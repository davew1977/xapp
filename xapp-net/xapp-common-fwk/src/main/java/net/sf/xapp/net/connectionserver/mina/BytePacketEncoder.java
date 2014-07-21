/*
 *
 * Date: 2011-jan-08
 * Author: davidw
 *
 */
package net.sf.xapp.net.connectionserver.mina;

import net.sf.xapp.net.common.framework.Message;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

public class BytePacketEncoder extends ProtocolEncoderAdapter
{
    public static final int HEADER_SIZE=4; // an int

    private final int maxMessageLength;

    public BytePacketEncoder(int maxMessageLength)
    {
        this.maxMessageLength = maxMessageLength;
    }

    @Override
    public void encode(IoSession session, Object m, ProtocolEncoderOutput out) throws Exception
    {
        Message message = (Message) m;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutput dao = new DataOutputStream(baos);

        message.writeData(dao);

        byte[] bytes  = baos.toByteArray();
        int messageSize = bytes.length + 4;

        if(messageSize>maxMessageLength)
        {
            throw new Exception(String.format("max message size exceeded, message: %s, " +
                    "max message size: %s, actual size: %s", message, maxMessageLength, messageSize));
        }

        ByteBuffer buf = ByteBuffer.allocate(messageSize + HEADER_SIZE, false);
        buf.putInt(messageSize);
        buf.putInt(message.type().getId());
        buf.put(bytes);
        buf.flip();
        out.write(buf);
    }
}
