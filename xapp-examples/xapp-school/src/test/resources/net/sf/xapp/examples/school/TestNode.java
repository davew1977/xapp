package net.sf.xapp.examples.school;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

import net.sf.xapp.net.api.channel.Channel;
import net.sf.xapp.net.api.lobbysessionmanager.LobbySessionManager;
import net.sf.xapp.net.client.tools.adminclient.Processor;
import net.sf.xapp.net.client.tools.adminclient.ScriptPreprocessor;
import net.sf.xapp.net.common.framework.Adaptor;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.types.MessageTypeEnum;
import net.sf.xapp.net.server.channels.ChannelImpl;
import net.sf.xapp.net.server.channels.UserLocator;
import net.sf.xapp.net.server.clustering.Node;
import net.sf.xapp.net.server.clustering.NodeInfoImpl;
import net.sf.xapp.net.server.framework.eventloop.EventLoopManager;
import net.sf.xapp.net.server.framework.eventloop.EventLoopMessageHandler;
import net.sf.xapp.net.server.lobby.LobbySessionManagerImpl;
import net.sf.xapp.net.server.playerrepository.SimpleUserStore;
import net.sf.xapp.net.server.repos.EntityRepository;
import net.sf.xapp.net.testharness.TestMessageSender;

public class TestNode implements Processor {
    private final Node node;
    private final TestMessageSender testMessageSender;
    private ScriptPreprocessor scriptPreprocessor;

    public TestNode(String backupDirOverride, String... configs) {
        this.node = new Node(backupDirOverride, configs);
        scriptPreprocessor = new ScriptPreprocessor(null);
        scriptPreprocessor.setProcessor(new Processor() {
            @Override
            public void exec(String message) {
                node.handle(message);
            }
        });
        testMessageSender = node.getBean(TestMessageSender.class);

    }

    @Override
    public void exec(String message) {
        scriptPreprocessor.exec(message);
    }

    public InMessage waitFor(MessageTypeEnum m, Object... propValuePairs) throws InterruptedException {
        return testMessageSender.waitFor(m, propValuePairs);
    }

    public List<InMessage> waitFor(int count, MessageTypeEnum m, Object... propValuePairs) throws InterruptedException {
        List<InMessage> result = new ArrayList<InMessage>();
        for (int i = 0; i < count; i++) {
            result.add(testMessageSender.waitFor(m, propValuePairs));
        }
        return result;
    }

    public void assertFiredInOrder(String... events) {
        testMessageSender.assertFiredInOrder(events);
    }

    public void assertFiredInAnyOrder(String... events) {
        testMessageSender.assertFiredInAnyOrder(events);
    }

    private <T, H> T getTarget(Class<H> lookupClass, Class<T> targetClass, String key) {
        Adaptor<H> adaptor = (Adaptor<H>) node.getBean(EntityRepository.class).find(lookupClass, key);
        EventLoopMessageHandler<H> eventLoopMessageHandler = (EventLoopMessageHandler<H>) adaptor.getDelegate();
        return (T) eventLoopMessageHandler.getDelegate();
    }

    public ChannelImpl getChannel(String key) {
        return getTarget(Channel.class, ChannelImpl.class, key);
    }

    public EventLoopManager getEventLoopManager() {
        return node.getBean(EventLoopManager.class);
    }

    public EntityRepository getEntityRepository() {
        return node.getBean(EntityRepository.class);
    }

    public UserLocator getPlayerLocator() {
        return node.getBean(UserLocator.class);
    }

    public SimpleUserStore getSimpleUserStore() {
        return node.getBean(SimpleUserStore.class);
    }

    public NodeInfoImpl getNodeInfo() {
        return node.getBean(NodeInfoImpl.class);
    }

    public LobbySessionManagerImpl getLobby(String key) {
        return getTarget(LobbySessionManager.class, LobbySessionManagerImpl.class, key);
    }

    public ClassPathXmlApplicationContext getAppContext() {
        return node.getAppContext();
    }
}
