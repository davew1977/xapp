package net.sf.xapp.objserver;

import net.sf.xapp.net.api.channel.Channel;
import net.sf.xapp.net.api.channel.ChannelAdaptor;
import net.sf.xapp.net.api.messagesender.MessageSender;
import net.sf.xapp.net.server.channels.ChannelImpl;
import net.sf.xapp.net.server.channels.UserLocator;
import net.sf.xapp.net.server.clustering.ClusterFacade;
import net.sf.xapp.net.server.framework.eventloop.EventLoopManager;
import net.sf.xapp.net.server.framework.eventloop.EventLoopMessageHandler;
import net.sf.xapp.net.server.repos.EntityRepository;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerAdaptor;

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
    private final SimpleXmlDatabase xmlDatabase;

    public ObjFactoryImpl(ClusterFacade clusterFacade,
                          EventLoopManager eventLoopManager,
                          EntityRepository entityRepository,
                          MessageSender messageSender,
                          UserLocator userLocator,
                          SimpleXmlDatabase xmlDatabase) {
        this.clusterFacade = clusterFacade;
        this.eventLoopManager = eventLoopManager;
        this.entityRepository = entityRepository;
        this.messageSender = messageSender;
        this.userLocator = userLocator;
        this.xmlDatabase = xmlDatabase;
    }

    @PostConstruct
    public void init() throws ClassNotFoundException {
        List<ObjInfo> objInfos = xmlDatabase.findAll();
        for (ObjInfo objInfo : objInfos) {
            create_internal(objInfo.getKey(), objInfo.getObjectMeta());
        }
    }

    public void create_internal(String key, ObjectMeta objectMeta) {
        ObjController objController = new ObjController(key, objectMeta);
        ChannelImpl channel = new ChannelImpl(messageSender, userLocator, objController);

        ChannelAdaptor channelEL = new ChannelAdaptor(key, new EventLoopMessageHandler<Channel>(eventLoopManager, channel));
        ObjManager objServerEL = new ObjManagerAdaptor(key, new EventLoopMessageHandler<ObjManager>(eventLoopManager, objController));

        entityRepository.add(ObjManager.class, key, objServerEL);
        entityRepository.add(Channel.class, key, channelEL);

        clusterFacade.addEntityMapping(key);
    }
}
