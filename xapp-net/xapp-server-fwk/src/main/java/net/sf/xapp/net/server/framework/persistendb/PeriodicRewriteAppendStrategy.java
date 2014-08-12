package net.sf.xapp.net.server.framework.persistendb;

import net.sf.xapp.net.common.framework.Entity;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.StringSerializable;
import net.sf.xapp.net.common.framework.TransportHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PeriodicRewriteAppendStrategy<E extends Entity,U> implements AppendStrategy<U>
{
    private final int maxAppendCount; //
    private final Map<String, AtomicInteger> appendCounters;
    private FileDBImpl<E,U> fileDB;

    public PeriodicRewriteAppendStrategy(FileDBImpl<E,U> fileDB, int maxAppendCount)
    {
        this.fileDB = fileDB;
        this.maxAppendCount = maxAppendCount;
        appendCounters = new HashMap<String, AtomicInteger>();
    }

    @Override
    public <T> T handleMessage(InMessage<U, T> utInMessage)
    {
        String key = utInMessage.entityKey();
        AtomicInteger counter = getCounter(key);
        if(counter.getAndIncrement() < maxAppendCount)
        {
            fileDB.fileSystem.append(key, TransportHelper.toString(utInMessage));
        }
        else
        {
            counter.set(0);
            PersistentObj<E> obj = fileDB.localCache.get(key);
            fileDB.add(key, obj.incrementSeqNo(maxAppendCount));
        }
        return null;
    }

    @Override
    public void processingAppend(String key, String updateLine)
    {
        getCounter(key).incrementAndGet();
    }

    private AtomicInteger getCounter(String key)
    {
        AtomicInteger counter = appendCounters.get(key);
        if(counter==null)
        {
            counter = new AtomicInteger(0);
            appendCounters.put(key, counter);
        }
        return counter;
    }
}
