package net.sf.xapp.net.server.framework;

import ngpoker.common.framework.Adaptor;
import ngpoker.common.framework.MessageHandler;

public class DecoratorHelper
{
    public static <T,A> T getCore(Class<T> targetClass, Class<A> interfaceClass, A wrapper)
    {
        if(targetClass.isInstance(wrapper))
        {
            return (T) wrapper;
        }
        if(wrapper instanceof Adaptor)
        {
            Adaptor<A> adaptor = (Adaptor<A>) wrapper;
            MessageHandler<A> mh = adaptor.getDelegate();
            if(mh instanceof Decorator)
            {
                Decorator<A> decorator = (Decorator<A>) mh;
                return getCore(targetClass, interfaceClass, decorator.getDelegate());
            }
            else
            {
                throw new RuntimeException( String.format("cannot proceed since %s is not a decorator ",mh));
            }
        }
        else if(wrapper instanceof Decorator)
        {
            Decorator<A> decorator = (Decorator<A>) wrapper;

            return getCore(targetClass, interfaceClass, decorator.getDelegate());

        }
        else
        {
            throw new RuntimeException( String.format("cannot find core targetClass:%s interfaceClass:%s wrappedObject:%s", targetClass, interfaceClass, wrapper));
        }
    }
}
