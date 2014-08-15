package net.sf.xapp.objserver;

import net.sf.xapp.net.api.messagesender.MessageSender;
import net.sf.xapp.net.server.channels.ChannelImpl;
import net.sf.xapp.net.server.channels.UserLocator;
import net.sf.xapp.net.server.clustering.ClusterFacade;
import net.sf.xapp.net.server.connectionserver.messagesender.MessageSender;
import net.sf.xapp.net.server.framework.eventloop.EventLoopManager;
import net.sf.xapp.net.server.framework.eventloop.EventLoopMessageHandler;
import net.sf.xapp.net.server.framework.persistendb.FileDB;
import net.sf.xapp.net.server.framework.persistendb.PersistentObj;
import net.sf.xapp.net.server.repos.EntityRepository;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import ngpoker.client.channel.Channel;
import ngpoker.client.channel.ChannelAdaptor;
import ngpoker.common.framework.Entity;
import ngpoker.common.framework.MessageHandler;
import ngpoker.common.util.ReflectionUtils;
import objserver.core.ObjControl;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class ObjFactoryImpl {
    private final ClusterFacade clusterFacade;
    private final EventLoopManager eventLoopManager;
    private final EntityRepository entityRepository;
    private final MessageSender messageSender;
    private final UserLocator userLocator;

    public ObjFactoryImpl(ClusterFacade clusterFacade,
                          EventLoopManager eventLoopManager,
                          EntityRepository entityRepository,
                          MessageSender messageSender,
                          UserLocator userLocator) {
        this.clusterFacade = clusterFacade;
        this.eventLoopManager = eventLoopManager;
        this.entityRepository = entityRepository;
        this.messageSender = messageSender;
        this.userLocator = userLocator;
    }

    @PostConstruct
    public void init() {
        List<PersistentObj<E>> all = objDb.readAllWithMeta();
        for (PersistentObj<E> obj : all) {
            create_internal(obj);
            System.out.println(obj.getEntity().expandToString());
        }
    }

    public void create_internal(String key, ObjectMeta objectMeta) {
        ObjController objController = new ObjController(key, objectMeta);





        E obj = persistentObj.getEntity();
        String key = obj.getKey();
        ReflectionUtils.call(obj, "addListener", createListenerAdaptor(key, objDb));
        ObjControl<E> objControl = new ObjControl<E>(obj, persistentObj.getSeqNo());
        ChannelImpl channel = new ChannelImpl(messageSender, userLocator, objControl);

        ChannelAdaptor channelEL = new ChannelAdaptor(key, new EventLoopMessageHandler<Channel>(eventLoopManager, channel));
        ObjServer objServerEL = new ObjServerAdaptor(key, new EventLoopMessageHandler<ObjServer>(eventLoopManager, objControl));

        entityRepository.add(ObjServer.class, key, objServerEL);
        entityRepository.add(Channel.class, key, channelEL);

        clusterFacade.addEntityMapping(key);
    }
}
