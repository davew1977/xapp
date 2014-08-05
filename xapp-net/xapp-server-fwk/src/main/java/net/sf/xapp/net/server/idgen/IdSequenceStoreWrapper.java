package net.sf.xapp.net.server.idgen;

import ngpoker.infrastructure.types.EntityType;
import ngpoker.infrastructure.types.IdSequence;
import ngpoker.infrastructure.types.IdSequenceStore;

public class IdSequenceStoreWrapper extends IdSequenceStore
{
    public IdSequenceStoreWrapper()
    {

    }

    public long getAndIncrement(EntityType entityType)
    {
        java.util.List<IdSequence> seqs = getSequences();
        for (int i = 0; i < seqs.size(); i++)
        {
            IdSequence idSequence = seqs.get(i);
            if (idSequence.getEntityType() == entityType)
            {
                Long seq = idSequence.getSeq();
                setSequenceSeq(i, seq + 1);
                return seq;
            }
        }
        throw new IllegalArgumentException("entity type " + entityType + " not handled");
    }

    public long get(EntityType entityType)
    {
        for (IdSequence idSequence : getSequences())
        {
            if(idSequence.getEntityType()==entityType)
            {
                return idSequence.getSeq();
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        IdSequenceStore idSequenceStore = new IdSequenceStore();
        for (EntityType entityType : EntityType.values()) {
            idSequenceStore.getSequences().add(new IdSequence(entityType));
        }

    }
}
