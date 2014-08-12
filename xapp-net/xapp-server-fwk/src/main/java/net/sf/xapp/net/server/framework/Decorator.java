package net.sf.xapp.net.server.framework;

import net.sf.xapp.net.common.framework.MessageHandler;

public interface Decorator<A> extends MessageHandler<A>
{
    A getDelegate();
}
