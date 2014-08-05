/*
 *
 * Date: 2011-feb-19
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.framework.persistendb;

import ngpoker.common.framework.Entity;
import ngpoker.common.framework.MessageHandler;
import ngpoker.common.types.MessageTypeEnum;

import java.util.List;

public interface FileDB<E extends Entity,U> extends MessageHandler<U>
{
    void add(String key, E obj);
    void add(String key, PersistentObj<E> obj);
    void remove(String key);

    List<E> readAll();
    List<PersistentObj<E>> readAllWithMeta();

    E get(String key);

    /**
     * each update will result in either:
     *      {@link net.sf.xapp.net.server.framework.persistendb.UpdateAction#APPEND} a new line added to the entity's file
     *      {@link net.sf.xapp.net.server.framework.persistendb.UpdateAction#IGNORE} the update is not written and will be lost with the jvm
     *      {@link net.sf.xapp.net.server.framework.persistendb.UpdateAction#REWRITE} the update will result in the file to be replaced with a state snapshot
     *
     * The action will typically default to APPEND
     *
     * @param messageType
     * @return
     */
    UpdateAction getUpdateAction(MessageTypeEnum messageType);

    int size();
}
