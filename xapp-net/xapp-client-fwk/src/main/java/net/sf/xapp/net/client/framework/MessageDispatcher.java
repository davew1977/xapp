/*
 *
 * Date: 2010-dec-07
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.framework;

import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * a message handler that can dispatch messages to many handlers, depending on the api
 */
public class MessageDispatcher implements MessageHandler
{
    private final Logger log = Logger.getLogger(getClass());
    private List<MessageHandler> genericHandlers;
    private Map<Class, List<MessageHandler>> messageHandlers;
    private Map<Class, List<Object>> delegates;
    private Map<Class, Map<String, List<Object>>> keyedDelegates;
    private Map<Class, Map<String, List<MessageHandler>>> keyedHandlers;

    public MessageDispatcher()
    {
        genericHandlers = new ArrayList<MessageHandler>();
        messageHandlers = new HashMap<Class, List<MessageHandler>>();
        delegates = new HashMap<Class, List<Object>>();
        keyedDelegates = new HashMap<Class, Map<String, List<Object>>>();
        keyedHandlers = new HashMap<Class, Map<String, List<MessageHandler>>>();
    }

    public void addGenericHandler(MessageHandler messageHandler)
    {
        genericHandlers.add(messageHandler);
    }

    public <A> void addHandler(Class<A> type, MessageHandler<A> messageHandler)
    {
        List<MessageHandler> handlers = messageHandlers.get(type);
        if(handlers==null)
        {
            handlers = new ArrayList<MessageHandler>();
            messageHandlers.put(type, handlers);
        }
        handlers.add(messageHandler);
    }

    public <A> void addDelegate(Class<A> type, A delegate)
    {
        List<Object> delegateList = delegates.get(type);
        if(delegateList==null)
        {
            delegateList = new ArrayList<Object>();
            delegates.put(type, delegateList);
        }
        delegateList.add(delegate);
    }

    public <A> void addDelegate(Class<A> type, String key, A delegate)
    {
        assert key!=null;
        Map<String, List<Object>> map = keyedDelegates.get(type);
        if (map == null)
        {
            map = new HashMap<>();
            keyedDelegates.put(type, map);
        }
        List<Object> delegates = map.get(key);
        if(delegates==null)
        {
            delegates = new ArrayList<>();
            map.put(key, delegates);
        }
        if (!delegates.contains(delegate)) {
            delegates.add(delegate);
        }
    }

    public <A> void addHandler(Class<A> type, String key, MessageHandler<A> handler)
    {
        Map<String, List<MessageHandler>> map = keyedHandlers.get(type);
        if (map == null)
        {
            map = new HashMap<>();
            keyedHandlers.put(type, map);
        }
        List<MessageHandler> handlers = map.get(key);
        if(handlers==null)
        {
            handlers = new ArrayList<>();
            map.put(key, handlers);
        }
        handlers.add(handler);
    }

    @Override
    public Object handleMessage(InMessage inMessage)
    {
        for (MessageHandler genericHandler : genericHandlers)
        {
            genericHandler.handleMessage(inMessage);
        }
        List<MessageHandler> handlers = getKeyedHandler(inMessage);
        boolean handled = false;
        if(handlers!=null)
        {
            for (MessageHandler messageHandler : handlers)
            {
                messageHandler.handleMessage(inMessage);
                handled = true;
            }
        }
        List<Object> keyedDelegates = getKeyedDelegate(inMessage);
        if(keyedDelegates!=null)
        {
            for (Object delegate : keyedDelegates)
            {
                inMessage.visit(delegate);
                handled = true;
            }
        }

        //otherwise fall through to generic handlers
        if(messageHandlers.containsKey(inMessage.api()))
        {
            handlers = messageHandlers.get(inMessage.api());
            for (MessageHandler messageHandler : handlers)
            {
                messageHandler.handleMessage(inMessage);
                handled = true;
            }
        }
        if(delegates.containsKey(inMessage.api()))
        {
            List<Object> delegateList = delegates.get(inMessage.api());
            for (Object delegate : delegateList)
            {
                inMessage.visit(delegate);
                handled = true;
            }
        }
        if(!handled)
        {
            log.debug("unhandled: " + inMessage);
        }
        return null;
    }

    private List<Object> getKeyedDelegate(InMessage inMessage)
    {
        Class api = inMessage.api();
        String key = inMessage.entityKey();
        Map<String, List<Object>> map = keyedDelegates.get(api);
        return map!=null ? map.get(key) : null;
    }

    private List<MessageHandler> getKeyedHandler(InMessage inMessage)
    {
        Class api = inMessage.api();
        String key = inMessage.entityKey();
        Map<String, List<MessageHandler>> map = keyedHandlers.get(api);
        return map!=null ? map.get(key) : null;
    }

    public void removeAllForKey(String key)
    {
        for (Map<String, List<Object>> delegateMap : keyedDelegates.values())
        {
            delegateMap.remove(key);
        }
        for (Map<String, List<MessageHandler>> handlerMap : keyedHandlers.values())
        {
            handlerMap.remove(key);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("generic handlers:\n");
        for (MessageHandler genericHandler : genericHandlers)
        {
            sb.append("\t").append(genericHandler).append("\n");
        }
        sb.append("delegates:\n");
        for (Map.Entry<Class, List<Object>> entry : delegates.entrySet())
        {
            sb.append("\t").append(entry.getKey()).append(":\n");
            for (Object delagate : entry.getValue())
            {
                sb.append("\t\t").append(delagate).append("\n");
            }
        }
        sb.append("message handlers:\n");
        for (Map.Entry<Class, List<MessageHandler>> entry : messageHandlers.entrySet())
        {
            sb.append("\t").append(entry.getClass()).append(":\n");
            for (MessageHandler messageHandler : entry.getValue())
            {
                sb.append("\t\t").append(messageHandler).append("\n");
            }
        }
        sb.append("keyed delegates:\n");
        for (Map.Entry<Class, Map<String, List<Object>>> entry : keyedDelegates.entrySet())
        {
            sb.append("\t").append(entry.getKey()).append("\n");
            for (Map.Entry<String, List<Object>> e : entry.getValue().entrySet())
            {
                sb.append("\t\t").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
        }
        sb.append("keyed message handlers:\n");
        for (Map.Entry<Class, Map<String, List<MessageHandler>>> entry : keyedHandlers.entrySet())
        {
            sb.append("\t").append(entry.getKey()).append("\n");
            for (Map.Entry<String, List<MessageHandler>> e : entry.getValue().entrySet())
            {
                sb.append("\t\t").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
        }
        return sb.toString();
    }

    public void clearStatefulHandlers()
    {
        keyedDelegates.clear();
        keyedHandlers.clear();
    }
}
