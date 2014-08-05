package net.sf.xapp.net.server.framework;

import ngpoker.common.framework.MessageHandler;

public interface Decorator<A> extends MessageHandler<A>
{
    A getDelegate();
}
