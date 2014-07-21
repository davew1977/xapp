/*
 *
 * Date: 2010-jun-29
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * multicasts a message to a set of delegates. Obviously this is only suitable for asynchronous apis
 *
 * this is not threadsafe, and must be sync'd on externally
 *
 * @param <A>
 */
public class Multicaster<A> implements MessageHandler<A>
{
    private List<A> delegates;
    private List<A> toRemove;

    public Multicaster()
    {
        this(new ArrayList<A>());
    }

    public Multicaster(List<A> delegates)
    {
        this.delegates = delegates;
    }

    public void addDelegate(A delegate)
    {
        assert delegate != null;
        delegates.add(delegate);
    }

    public void removeDelegate(A delegate)
    {
        if(toRemove==null)
        {
            toRemove = new ArrayList<A>();
        }
        toRemove.add(delegate);
    }

    public void removeAllDelegates()
    {
        toRemove = new ArrayList<A>(delegates);
    }

    public void setDelegates(List<A> delegates)
    {
        this.delegates = delegates;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> inMessage)
    {
        tryRemove();
        for (A delegate : delegates)
        {
            inMessage.visit(delegate);
        }
        tryRemove();
        return null;
    }

    private void tryRemove()
    {
        if(toRemove!=null)
        {
            delegates.removeAll(toRemove);
            toRemove = null;
        }
    }

    public static <T> Multicaster<T> wrapInMulticaster(T... delegates)
    {
        Multicaster<T> channelMulticaster = new Multicaster<T>();
        for (T t : delegates)
        {
            channelMulticaster.addDelegate(t);
        }
        return channelMulticaster;
    }
}
