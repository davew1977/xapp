/*
 *
 * Date: 2011-feb-19
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.persistendb;

import net.sf.xapp.utils.StringUtils;
import ngpoker.Version;
import net.sf.xapp.net.common.framework.Entity;
import ngpoker.common.types.MessageTypeEnum;
import net.sf.xapp.net.server.clustering.NodeInfo;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.StringSerializable;
import net.sf.xapp.net.common.framework.TransportHelper;
import ngpoker.common.util.ReflectionUtils;
import net.sf.xapp.net.server.framework.smartconverter.ConvertResult;
import net.sf.xapp.net.server.util.filesystemstore.FileContent;
import net.sf.xapp.net.server.util.filesystemstore.FileSystem;
import net.sf.xapp.net.server.util.filesystemstore.RealFileSystemFactory;
import org.apache.log4j.Logger;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO revise local cache, should remove from this implementation
 * @param <E>
 * @param <U>
 */
public class FileDBImpl<E extends Entity,U> implements FileDB<E, U>
{
    private final Logger log = Logger.getLogger(getClass());
    private final FileContentConverter fileContentConverter;
    private final Map<MessageTypeEnum, UpdateAction> actionMap;
    private final Class<E> entityClass;
    private final String dataClassName; //the name of the generated observable type
    protected final FileSystem fileSystem;

    protected final Map<String, PersistentObj<E>> localCache;
    private final AppendStrategy<U> appendStrategy;


    public FileDBImpl(NodeInfo nodeInfo, Class<E> aClass,
                      String partitionName,
                      FileContentConverter fileContentConverter,
                      Class dataClass)
    {
        this.entityClass= aClass;
        this.fileContentConverter = fileContentConverter;
        this.dataClassName = dataClass.getName();
        this.actionMap = new HashMap<MessageTypeEnum, UpdateAction>();

        fileSystem = new RealFileSystemFactory().create(nodeInfo, partitionName);

        localCache = new HashMap<String,PersistentObj<E>>();

        appendStrategy = new PeriodicRewriteAppendStrategy<E,U>(this, 4);
    }

    @Override
    public synchronized void add(String key, E obj)
    {
        add(key, new PersistentObj<E>(obj, 0));
    }
    @Override
    public synchronized void add(String key, PersistentObj<E> obj)
    {
        fileSystem.createFile(key, Version.VERSION + "," + dataClassName + "," + obj.getSeqNo(), obj.serialize());
        localCache.put(key, obj);
    }

    @Override
    public synchronized void remove(String key)
    {
        fileSystem.deleteFile(key);
        localCache.remove(key);
    }

    @PreDestroy
    public void destroy() throws IOException
    {
        fileSystem.destroy();
    }


    @Override
    public synchronized List<E> readAll()
    {
        List<E> result = new ArrayList<E>();
        List<String> paths = fileSystem.filePaths();
        for (String path : paths)
        {
            E obj = read(path).getEntity();

            result.add(obj);
        }
        return result;
    }

    @Override
    public synchronized List<PersistentObj<E>> readAllWithMeta() {
        List<PersistentObj<E>> result = new ArrayList<PersistentObj<E>>();
        List<String> paths = fileSystem.filePaths();
        for (String path : paths)
        {
            PersistentObj<E> obj = read(path);
            result.add(obj);
        }
        return result;
    }

    private PersistentObj<E> read(String path) {
        FileContent fileContent = fileSystem.readFile(path);
        String key = StringUtils.leaf(path, "/");

        //handle version conversion
        ConvertResult<FileContent> convertResult = fileContentConverter.convert(fileContent);
        fileContent = convertResult.getTarget();

        PersistentObj<E> obj;
        if(convertResult.isConverted())
        {
            obj = parseObj(fileContent);
            log.info("writing converted file back to disk: " + obj);
            add(key, obj);
        }
        else
        {
            obj = parseAndCacheObj(fileContent, key);
        }
        return obj;
    }

    private PersistentObj<E> parseAndCacheObj(FileContent fileContent, String key)
    {
        List<String> updateLines = fileContent.linesAfterFirst();
        PersistentObj<E> obj = parseObj(fileContent);
        for (String updateLine : updateLines)
        {
            appendStrategy.processingAppend(key, updateLine);
        }
        localCache.put(key, obj);
        return obj;
    }

    private PersistentObj<E> parseObj(FileContent fileContent)
    {
        List<String> updateLines = fileContent.linesAfterFirst();
        E obj = ReflectionUtils.newInstance(entityClass);
        obj.deserialize(fileContent.firstLine());
        obj.init();
        long seqNo = fileContent.getBaseSeqNo();
        for (String updateLine : updateLines)
        {
            InMessage<U, Void> update = TransportHelper.fromString(updateLine);
            update.visit((U) obj);
            seqNo++;
        }
        return new PersistentObj<E>(obj, seqNo);
    }

    @Override
    public E get(String key)
    {
        PersistentObj<E> e = localCache.get(key);
        if(e==null)
        {
            FileContent fileContent = fileSystem.readFileFromKey(key);
            return parseAndCacheObj(fileContent, key).getEntity();
        }
        return e.getEntity();
    }

    @Override
    public synchronized <T> T handleMessage(InMessage<U, T> utInMessage)
    {
        return appendStrategy.handleMessage(utInMessage);
    }

    @Override
    public UpdateAction getUpdateAction(MessageTypeEnum messageType)
    {
        return actionMap.get(messageType);
    }

    @Override
    public int size()
    {
        return localCache.size();
    }

}
