/*
 *
 * Date: 2010-sep-09
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import net.sf.xapp.utils.ReflectionUtils;
import net.sf.xapp.net.common.framework.InMessage;
import net.sf.xapp.net.common.framework.TransportHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Node
{
    private final Logger log = Logger.getLogger(getClass());
    private static final String FAKE_CASHGAME_FEED_BEAN = "fakeCashGameLobbyFeed";
    private static final String FAKE_SNG_FEED_BEAN = "fakeSngLobbyFeed";
    private static final String CASH_GAME_FACTORY = "cashgameFactory";
    private static final String USER_STORE= "userStore";
    private static final String MONEY_SESSION_REFUNDER = "moneySessionRefunder";

    private static final String[] OPTIONAL_BEANS = new String[]{
            FAKE_CASHGAME_FEED_BEAN,
            FAKE_SNG_FEED_BEAN,
            USER_STORE,
            MONEY_SESSION_REFUNDER, //money session refunder must initialise before the cashgame store and after the user store
            CASH_GAME_FACTORY};       //

    private ClassPathXmlApplicationContext appContext;

    public Node(String backupDirOverride, String... configs)
    {
        appContext = new ClassPathXmlApplicationContext();
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        Properties properties = loadProperties();
        configurer.setProperties(properties);
        if(backupDirOverride != null) {
            properties.setProperty("backup.dir", backupDirOverride);
        }
        appContext.addBeanFactoryPostProcessor(configurer);
        appContext.setConfigLocations(configs);
        appContext.registerShutdownHook();
        appContext.refresh();

        NodeQThread nodeQThread = (NodeQThread) appContext.getBean("nodeQThread");
        nodeQThread.init();
        for (String beanName : OPTIONAL_BEANS)
        {
            Object bean = null;
            try
            {
                bean = appContext.getBean(beanName);
                ReflectionUtils.call(bean, "init");
            }
            catch (BeansException e)
            {
                log.info("optional bean "+ beanName + " does not exist");
            }
        }
    }

    public static void main(String[] args)
    {
        List<String> confs = new ArrayList<String>();
        confs.add("/spring/basic-node.xml");
        confs.add("/spring/hazelcast.xml");
        confs.add("/spring/channels.xml");
        confs.add("/spring/chat.xml");
        confs.add("/spring/connection-server.xml");
        confs.add("/spring/admin-server.xml");
        confs.add("/spring/lobby.xml");
        confs.add("/spring/cash-game.xml");
        confs.add("/spring/tournament.xml");
        confs.add(String.format("/spring/node-%s.xml", args[0]));
        confs.add("/spring/user-stats-mailer.xml");
        confs.add("/spring/forum.xml");

        new Node(null, confs.toArray(new String[confs.size()]));
    }

    public void handle(String cmd)
    {
        PublicEntryPoint pep = getBean(PublicEntryPoint.class);
        pep.handleMessage(TransportHelper.<InMessage>fromString(cmd));
    }
    public static Properties loadProperties()
    {
        Properties props = new Properties();
        try
        {
            props.load(Node.class.getResourceAsStream("/ngpoker.properties"));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return props;
    }

    public <T> T getBean(Class<T> aClass)
    {
        return (T) appContext.getBeansOfType(aClass).values().iterator().next();
    }

    public ClassPathXmlApplicationContext  getAppContext()
    {
        return appContext;
    }
}
