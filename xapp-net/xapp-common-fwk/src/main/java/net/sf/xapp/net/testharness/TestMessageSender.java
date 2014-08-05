package net.sf.xapp.net.testharness;

import net.sf.xapp.utils.ReflectionUtils;
import net.sf.xapp.net.common.framework.InMessage;
import ngpoker.common.types.MessageTypeEnum;
import net.sf.xapp.net.common.util.GeneralUtils;
import net.sf.xapp.net.common.util.StringUtils;
import net.sf.xapp.net.server.connectionserver.messagesender.MessageSender;
import net.sf.xapp.net.server.connectionserver.messagesender.MessageSenderAdaptor;
import net.sf.xapp.net.server.connectionserver.messagesender.to.Broadcast;
import net.sf.xapp.net.server.connectionserver.messagesender.to.Post;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TestMessageSender extends MessageSenderAdaptor
{
    private final Logger log = Logger.getLogger(getClass());

    private LinkedBlockingQueue<InMessage> outMessages;
    private List<String> messagesAsString;

    public TestMessageSender()
    {
        outMessages = new LinkedBlockingQueue<InMessage>();
        messagesAsString = new ArrayList<String>();
    }

    @Override
    public synchronized <T> T handleMessage(InMessage<MessageSender, T> inMessage)
    {
        log.info(inMessage.serialize());
        messagesAsString.add(inMessage.serialize());
        outMessages.add(inMessage);
        return null;
    }

    public InMessage waitFor(MessageTypeEnum m, Object... propValuePairs) throws InterruptedException
    {
        while (true)
        {
            InMessage inMessage = outMessages.poll(5000, TimeUnit.MILLISECONDS);
            if (inMessage == null)
            {
                throw new RuntimeException(
                        String.format("timed out waiting for %s %s", m, Arrays.toString(propValuePairs)));
            }
            InMessage wrappedMessage = wrappedMessage(inMessage);
            boolean match = wrappedMessage.type() == m;
            if (match)
            {
                String propName = null;
                for (int i = 0; i < propValuePairs.length; i++)
                {
                    Object item = propValuePairs[i];
                    if(i%2==0)
                    {
                        propName = (String) item;
                    }
                    else
                    {
                        match &= matches(wrappedMessage, propName, item);
                    }
                }
                if (match)
                {
                    return wrappedMessage;
                }
            }
        }
    }

    private boolean matches(InMessage wrappedMessage, String propName, Object expected)
    {
        String methodName = "get" + StringUtils.capitalizeFirst(propName);
        if (ReflectionUtils.findMatchingMethod(wrappedMessage.getClass(), methodName) != null)
        {
            Object actual = ReflectionUtils.call(wrappedMessage, methodName);
            return GeneralUtils.objEquals(expected, actual);
        }
        return false;
    }


    private InMessage wrappedMessage(InMessage inMessage)
    {
        if (inMessage instanceof Post)
        {
            return (InMessage) ((Post) inMessage).getMessage();
        }
        else
        {
            return (InMessage) ((Broadcast) inMessage).getMessage();
        }
    }

    public synchronized void assertFiredInAnyOrder(String... events)
    {
        assertFiredInAnyOrder(messagesAsString, events);
    }
    public synchronized void assertFiredInOrder(String... events)
    {
        assertFiredInOrder(messagesAsString, events);
    }

    public static void assertFiredInAnyOrder(List<String> messagesAsString, String... events)
    {
        List<String> matchExprs = new ArrayList<String>(Arrays.asList(events));

        for (String event : events)
        {
            for (String s : messagesAsString)
            {
                if(s.contains(event))
                {
                    matchExprs.remove(event);
                }
            }
        }
        if(!matchExprs.isEmpty())
        {
            throw new AssertionError( String.format("events not found in any order: " + matchExprs + ", actual: " + messagesAsString));
        }
    }
    public static void assertFiredInOrder(List<String> messagesAsString, String... events)
    {
        LinkedList<String> matchExprs = new LinkedList<String>(Arrays.asList(events));
        for (String message : messagesAsString)
        {
            if(!matchExprs.isEmpty())
            {
                if(message.matches(matchExprs.getFirst()))
                {
                    matchExprs.removeFirst();
                }
            }
        }
        if(!matchExprs.isEmpty())
        {
            throw new AssertionError( String.format("events not found in order: " + matchExprs));
        }
    }
}
