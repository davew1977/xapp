package net.sf.xapp.net.server.framework.persistendb;

import net.sf.xapp.net.common.framework.Entity;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 4/16/14
 * Time: 5:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class PersistentObj<E extends Entity> {
    private final E entity;
    private final long seqNo;

    public PersistentObj(E entity, long seqNo) {
        this.entity = entity;
        this.seqNo = seqNo;
    }

    public E getEntity() {
        return entity;
    }

    public long getSeqNo() {
        return seqNo;
    }

    public String serialize() {
        return entity.serialize();
    }

    public PersistentObj<E> incrementSeqNo(int maxAppendCount) {
        return new PersistentObj<E>(entity, seqNo + maxAppendCount);
    }
}
