package net.sf.xapp.net.common.framework;

public interface Adaptor<T>
{
    MessageHandler<T> getDelegate();
    
}
