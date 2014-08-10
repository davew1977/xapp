package net.sf.xapp.net.connectionserver.mina;

import net.sf.xapp.Global;
import net.sf.xapp.net.api.exampleapi.to.DoSomething;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * BytePacketDecoder Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/18/2011</pre>
 */
public class BytePacketCodecTest {

    @Test
    public void testCodec() throws Exception {
        BytePacketCodecFactory sf = new BytePacketCodecFactory(true);
        BytePacketCodecFactory cf = new BytePacketCodecFactory(false);

        ProtocolEncoder encoder = sf.getEncoder();
        BytePacketDecoder decoder = (BytePacketDecoder) cf.getDecoder();

        DoSomething m = new DoSomething("woowowow", 2992833L,Arrays.asList(1,2,3,4,56));
        ProtocolEncoderOutputImpl output = new ProtocolEncoderOutputImpl();
        encoder.encode(null, m, output);

        ByteBuffer buf = output.buffer;

        assertEquals(46, buf.getInt(0));
        assertEquals(50, buf.limit());

        byte[] bytes = new byte[42];
        assertEquals(46, buf.getInt());
        int messageType = buf.getInt();
        DoSomething message = (DoSomething) Global.create(messageType);
        buf.get(bytes);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        message.readData(new DataInputStream(bis));

        assertEquals(message.toString(), m.toString());
        assertEquals(50, buf.limit());


        buf.flip();
        assertEquals(50, buf.limit());
        assertEquals(0, buf.position());

        ProtocolDecoderOutputImpl protocolDecoderOutput = new ProtocolDecoderOutputImpl();
        decoder.doDecode(null, buf, protocolDecoderOutput);
        assertEquals(protocolDecoderOutput.m.toString(), message.toString());
    }

    private static class ProtocolEncoderOutputImpl implements ProtocolEncoderOutput {
        ByteBuffer buffer;

        @Override
        public void write(ByteBuffer buf) {
            buffer = buf;
        }

        @Override
        public void mergeAll() {

        }

        @Override
        public WriteFuture flush() {
            return null;
        }
    }

    private static class ProtocolDecoderOutputImpl implements ProtocolDecoderOutput {
        Object m;

        @Override
        public void write(Object message) {
            m = message;
        }

        @Override
        public void flush() {

        }
    }
}
