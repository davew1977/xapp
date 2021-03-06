/*
 *
 * Date: 2010-okt-25
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.net.server.repos.EntityRepository;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles all async messages directed at this node
 */
public class NodeAsyncMessageHandler implements MessageHandler
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BeanManager beanManager;
    private final EntityRepository entityRepository;

    public NodeAsyncMessageHandler(BeanManager beanManager, EntityRepository entityRepository)
    {
        this.beanManager = beanManager;
        this.entityRepository = entityRepository;
    }

    @Override
    public Object handleMessage(InMessage message)
    {
        String key = message.entityKey();
        Class api = message.api();
        if(key !=null)
        {
            //entity must exist on node
            Object entity = entityRepository.find(api, key);
            if (entity!=null)
            {
                message.visit(entity);
            }
            else
            {
                log.info(String.format("entity not registered with key %s and type %s", key, api));
            }
        }
        else
        {
            MessageHandler bean = beanManager.findBean(api);
            bean.handleMessage(message);
        }
        return null;
    }
}
