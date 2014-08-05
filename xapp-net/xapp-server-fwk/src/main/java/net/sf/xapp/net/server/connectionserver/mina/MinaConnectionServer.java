/*
 *
 * Date: 2010-sep-17
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.connectionserver.mina;

import net.sf.xapp.net.server.clustering.NodeInfo;
import net.sf.xapp.net.server.clustering.NodeInfoImpl;
import org.apache.log4j.Logger;
import org.apache.mina.common.*;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetSocketAddress;

public class MinaConnectionServer
{
    private Logger log = Logger.getLogger(getClass());
    private final IoHandler ioHandler;
    private final int port;
    private SocketAcceptor acceptor;

    public MinaConnectionServer(IoHandler ioHandler, int port, NodeInfo nodeInfo)
    {
        this.ioHandler = ioHandler;
        this.port = port + nodeInfo.getNodeIndex();
    }

    @PostConstruct
    public void init() throws IOException
    {
        log.info("Initializing mina server on port " + port + "; using "+ioHandler);

        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

        acceptor = new SocketAcceptor();

        SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        //cfg.getFilterChain().addLast( "logger", new LoggingFilter());
        ProtocolCodecFilter protocolCodecFilter = new ProtocolCodecFilter(new BytePacketCodecFactory(true))
        {
            @Override
            public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception
            {
                super.exceptionCaught(nextFilter, session, cause);
                if(cause instanceof IOException && cause.getMessage().equals(
                        "An existing connection was forcibly closed by the remote host"))
                {
                   log.info(session.getAttribute("sessionKey") + " forcibly disconnected, " + session);
                }
                else
                {
                    log.error("exception in codec layer: ", cause);
                }
            }
        };
        cfg.getFilterChain().addLast( "codec", protocolCodecFilter);

        acceptor.bind( new InetSocketAddress(port), ioHandler, cfg);

        log.info("Initialized mina server on port " + port + "; using "+ioHandler);
    }

    @PreDestroy
    public void destroy()
    {
        acceptor.unbindAll();
    }

    public static void main(String[] args) throws IOException
    {
        new MinaConnectionServer(new IoHandlerAdapter()
        {
            
            @Override
            public void messageReceived(IoSession session, Object message) throws Exception
            {
                System.out.println(message);
                session.write(message);
            }

        }, 1137, new NodeInfoImpl(0, "")).init();
    }
}
