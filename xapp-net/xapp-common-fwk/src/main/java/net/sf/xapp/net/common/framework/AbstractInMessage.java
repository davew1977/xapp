/*
 *
 * Date: 2010-okt-01
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;


import ngpoker.common.types.ApiType;
import ngpoker.common.types.MessageTypeEnum;

public abstract class AbstractInMessage<I,V> extends AbstractObject implements InMessage<I,V>
{
    private final Class<I> api;
    private final ObjectType type;
    private final boolean persistent;
    protected String key;

    protected AbstractInMessage(Class<I> api, ObjectType type, boolean persistent)
    {
        this.api = api;
        this.type = type;
        this.persistent = persistent;
    }

    @Override
    public String entityKey()
    {
        return null;
    }

    @Override
    public ApiType apiType()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectType type()
    {
        return type;
    }

    @Override
    public Object principal()
    {
        return null;
    }

    @Override
    public boolean isPersistent()
    {
        return persistent;
    }

    @Override
    public Class<I> api()
    {
        return api;
    }

    @Override
    public String messageType() {
        return type().toString().split("_")[1];
    }
}
