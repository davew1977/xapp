/*
 *
 * Date: 2010-sep-14
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.repos;

import ngpoker.common.framework.InMessage;
import ngpoker.common.framework.MessageHandler;

public class EntityLookupMessageHandler<A> implements MessageHandler<A>
{
    private final EntityRepository entityRepository;

    public EntityLookupMessageHandler(EntityRepository entityRepository)
    {
        this.entityRepository = entityRepository;
    }

    @Override
    public <T> T handleMessage(InMessage<A, T> inMessage)
    {
        A entity = entityRepository.find(inMessage.api(), inMessage.entityKey());
        if(entity==null)
        {
            throw new RuntimeException(String.format("entity of type %s with key %s not found",
                    inMessage.api(), inMessage.entityKey()));
        }
        return inMessage.visit(entity);
    }
}
