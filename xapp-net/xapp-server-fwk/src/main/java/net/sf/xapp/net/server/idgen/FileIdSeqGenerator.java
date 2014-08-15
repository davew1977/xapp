package net.sf.xapp.net.server.idgen;

import net.sf.xapp.net.common.types.EntityType;
import net.sf.xapp.net.common.types.IdSequence;
import net.sf.xapp.net.common.types.IdSequenceStoreListener;
import net.sf.xapp.net.common.types.IdSequenceStoreListenerAdaptor;
import net.sf.xapp.net.server.clustering.NodeInfo;
import net.sf.xapp.net.server.framework.persistendb.FileDB;

import java.util.List;

public class FileIdSeqGenerator implements IdSeqGenerator
{
    private final NodeInfo nodeInfo;
    private final IdSequenceStoreWrapper idSequenceStore;

    public FileIdSeqGenerator(NodeInfo nodeInfo, IdSequenceStoreWrapper idSequenceStore)
    {
        this.nodeInfo = nodeInfo;
        this.idSequenceStore = idSequenceStore;
    }

    public FileIdSeqGenerator(NodeInfo nodeInfo)
    {
        this(nodeInfo, createStore());
    }
    
    public FileIdSeqGenerator(NodeInfo nodeInfo, FileDB<IdSequenceStoreWrapper, IdSequenceStoreListener> idSeqDB)
    {
        this(nodeInfo, getIdSequenceStore(idSeqDB));
    }

    private static IdSequenceStoreWrapper getIdSequenceStore(
            FileDB<IdSequenceStoreWrapper, IdSequenceStoreListener> idSeqDB)
    {
        List<IdSequenceStoreWrapper> idSequenceStores = idSeqDB.readAll();
        IdSequenceStoreWrapper store;
        if(idSequenceStores.isEmpty())
        {
            store = createStore();
            idSeqDB.add("1", store);
        }
        else
        {
            store = idSeqDB.readAll().get(0);
        }
        store.addListener(new IdSequenceStoreListenerAdaptor("1", idSeqDB));
        return store;
    }

    private static IdSequenceStoreWrapper createStore()
    {
        IdSequenceStoreWrapper store;
        store = new IdSequenceStoreWrapper();
        for (EntityType entityType : EntityType.values())
        {
            store.getSequences().add(new IdSequence(entityType));
        }
        return store;
    }

    @Override
    public synchronized String nextId(EntityType entityType)
    {
        return nodeInfo.getMyNodeId().getValue() + "_" + abbreviation(entityType) + "_" +
                idSequenceStore.getAndIncrement(entityType);
    }

    private String abbreviation(EntityType entityType)
    {
        return entityType==EntityType.forum_thread ? "ft" :
                entityType==EntityType.forum_post ? "fp" :
                        String.valueOf(entityType.name().charAt(0));
    }

    @Override
    public long peek(EntityType entityType)
    {
        return idSequenceStore.get(entityType);
    }
}
