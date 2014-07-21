/*
 *
 * Date: 2010-nov-03
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework.scheduling;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.testharness.TestMessageSender;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Implementation that invokes the method directly, in the same thread
 * @param <A>
 */
public class DummyScheduledMessageHandler<A> implements ScheduledMessageHandler<A>
{
    private boolean blocked;
    private Queue<InMessage<A, Void>> latestTasks;
    private A api;

    public DummyScheduledMessageHandler()
    {
        latestTasks = new LinkedList<InMessage<A, Void>>();
    }

    @Override
    public void init(A api)
    {
        this.api = api;
    }

    public void block()
    {
        blocked = true;
    }

    public void unblock()
    {
        flush();
        blocked = false;
    }

    public void flush()
    {
        Queue<InMessage<A,Void>> tmp = latestTasks;
        latestTasks = new LinkedList<InMessage<A, Void>>();
        while(!tmp.isEmpty())
        {
            consumeFrom(tmp);
        }
    }

    /**
     * keeps flushing until the real queue is empty. The queue could have one event, which, when flushed will cause another
     * event to be added to the queue, this event would also be flushed.
     *
     * The "flush" method will copy the the queue in order to flush only those events in the queue when the method was invoked
     */
    public void deepFlush()
    {
        while(!latestTasks.isEmpty())
        {
            consumeFrom(latestTasks);
        }
    }

    @Override
    public Task invokeLater(final InMessage<A, Void> message, long delay)
    {
        if(!blocked)
        {
            message.visit(api);
        }
        else
        {
            latestTasks.add(message);
        }
        return new Task()
        {
            @Override
            public boolean cancel()
            {
                return latestTasks.remove(message);
            }
        };
    }

    public void consumeFrom(Queue<InMessage<A,Void>> tasks)
    {
        assert !tasks.isEmpty();
        InMessage<A, Void> m = tasks.poll();
        System.out.println("consuming: " + m);
        m.visit(api);
    }

    public int size()
    {
        return latestTasks.size();
    }

    public void assertFiredInOrder(String... events)
    {
        List<String> messagesAsString = messageStringList();
        TestMessageSender.assertFiredInAnyOrder(messagesAsString, events);
    }

    public void assertFiredInAnyOrder(String... events)
    {
        List<String> stringList = messageStringList();
        TestMessageSender.assertFiredInAnyOrder(stringList, events);
    }

    private List<String> messageStringList()
    {
        List<String> messagesAsString = new ArrayList<String>();
        for (InMessage<A, Void> latestTask : latestTasks)
        {
            messagesAsString.add(latestTask.toString());
        }
        return messagesAsString;
    }

    @Override
    public String toString()
    {
        return latestTasks.toString();
    }
}
