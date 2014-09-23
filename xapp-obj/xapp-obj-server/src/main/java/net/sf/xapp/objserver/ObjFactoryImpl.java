package net.sf.xapp.objserver;

import net.sf.xapp.net.api.channel.Channel;
import net.sf.xapp.net.api.channel.ChannelAdaptor;
import net.sf.xapp.net.api.chatapp.ChatApp;
import net.sf.xapp.net.api.chatapp.ChatAppAdaptor;
import net.sf.xapp.net.api.messagesender.MessageSender;
import net.sf.xapp.net.api.userlookup.UserLookup;
import net.sf.xapp.net.server.channels.ChannelImpl;
import net.sf.xapp.net.server.channels.UserLocator;
import net.sf.xapp.net.server.chat.ChatAppImpl;
import net.sf.xapp.net.server.clustering.ClusterFacade;
import net.sf.xapp.net.server.framework.eventloop.EventLoopManager;
import net.sf.xapp.net.server.framework.eventloop.EventLoopMessageHandler;
import net.sf.xapp.net.server.repos.EntityRepository;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objserver.apis.objmanager.ObjManager;
import net.sf.xapp.objserver.apis.objmanager.ObjManagerAdaptor;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdate;
import net.sf.xapp.objserver.apis.objmanager.ObjUpdateAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class ObjFactoryImpl {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ClusterFacade clusterFacade;
    private final EventLoopManager eventLoopManager;
    private final EntityRepository entityRepository;
    private final MessageSender messageSender;
    private final UserLocator userLocator;
    private final SimpleXmlDatabase xmlDatabase;
    private final UserLookup userLookup;

    public ObjFactoryImpl(ClusterFacade clusterFacade,
                          EventLoopManager eventLoopManager,
                          EntityRepository entityRepository,
                          MessageSender messageSender,
                          UserLocator userLocator,
                          SimpleXmlDatabase xmlDatabase, UserLookup userLookup) {
        this.clusterFacade = clusterFacade;
        this.eventLoopManager = eventLoopManager;
        this.entityRepository = entityRepository;
        this.messageSender = messageSender;
        this.userLocator = userLocator;
        this.xmlDatabase = xmlDatabase;
        this.userLookup = userLookup;
    }

    @PostConstruct
    public void init() throws ClassNotFoundException {
        List<ObjInfo> objInfos = xmlDatabase.findAll();
        for (ObjInfo objInfo : objInfos) {
            create_internal(objInfo.getKey(), objInfo.getObjectMeta());
            log.info("creating live object: {}", objInfo.getKey());
        }
    }

    public void create_internal(String key, ObjectMeta objectMeta) {
        ObjController objController = new ObjController(key, objectMeta);
        ChatAppImpl chatApp = new ChatAppImpl(userLookup, key);
        ChannelImpl channel = new ChannelImpl(messageSender, userLocator, objController, chatApp);

        ChannelAdaptor channelEL = new ChannelAdaptor(key,
                new EventLoopMessageHandler<Channel>(eventLoopManager, channel));
        ChatApp chatEl = new ChatAppAdaptor(key,
                new EventLoopMessageHandler<ChatApp>(eventLoopManager, chatApp));
        ObjManager objServerEL = new ObjManagerAdaptor(key,
                new EventLoopMessageHandler<ObjManager>(eventLoopManager, objController));
        ObjUpdate objUpdateEL= new ObjUpdateAdaptor(key,
                new EventLoopMessageHandler<ObjUpdate>(eventLoopManager, objController.getLiveObject()));

        entityRepository.add(ObjManager.class, key, objServerEL);
        entityRepository.add(ObjUpdate.class, key, objUpdateEL);
        entityRepository.add(Channel.class, key, channelEL);
        entityRepository.add(ChatApp.class, key, chatEl);

        clusterFacade.addEntityMapping(key);
    }
}
