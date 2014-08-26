package net.sf.xapp.net.server.lobby;

import net.sf.xapp.net.api.lobbyinternal.LobbyInternal;
import net.sf.xapp.net.api.userlookup.UserLookup;
import net.sf.xapp.net.common.types.*;
import net.sf.xapp.net.server.clustering.ClusterFacade;
import net.sf.xapp.net.server.framework.email.MailProxy;
import net.sf.xapp.net.server.framework.eventloop.EventLoopManager;
import net.sf.xapp.net.server.framework.eventloop.EventLoopMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class UserStatsMailer implements LobbyInternal
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MailProxy mailProxy;
    private final ScheduledExecutorService scheduledExecutorService;
    private final UserLookup userLookup;
    private final List<String> stats;
    private final Set<String> excludedPids;

    public UserStatsMailer(String lobbyName,
                           MailProxy mailProxy,
                           ClusterFacade clusterFacade,
                           EventLoopManager eventLoopManager,
                           ScheduledExecutorService scheduledExecutorService,
                           UserLookup userLookup)
    {
        this.mailProxy = mailProxy;
        this.userLookup = userLookup;
        this.stats = new ArrayList<String>();
        this.scheduledExecutorService = scheduledExecutorService;
        clusterFacade.addTopicListener(lobbyName,
                new EventLoopMessageHandler<LobbyInternal>(eventLoopManager, this, "USER_STATS_MAILER"));

        excludedPids = new HashSet<String>();
        for(int i=0; i<100; i++)
        {
            excludedPids.add("0_p_" + i);
        }

        scheduleMail();
    }

    @Override
    public synchronized void entityAdded(String entityKey, LobbyEntity entity)
    {
    }

    @Override
    public synchronized void entityRemoved(String entityKey)
    {

    }

    @Override
    public synchronized void propertyChanged(String entityKey, LobbyPropertyEnum property, String value)
    {
        if(property==LobbyPropertyEnum.lastLoginTime && !excludedPids.contains(entityKey))
        {
            recordLogin(entityKey);
        }
    }

    @Override
    public void listPropertyChanged(String entityKey, LobbyPropertyEnum property, Integer index, String value,  ListOp listOp)
    {

    }

    private void recordLogin(String entityKey)
    {
        String timestamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
        User user = userLookup.findUser(new UserId(entityKey)).getUser();
        String nickname = user.getUsername();
        Country country = user.getCountry();
        String stat = String.format("%s - %s (%s from %s) logged in", timestamp, entityKey, nickname, country);
        log.info(stat);
        stats.add(stat);
    }

    private synchronized void scheduleMail()
    {
        scheduledExecutorService.schedule(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (!stats.isEmpty())
                    {
                        log.info("sending email of stats to dave");
                        StringBuilder content = new StringBuilder();
                        for (String stat : stats)
                        {
                            content.append(stat).append("\n");
                        }
                        mailProxy.sendMail(content.toString(), "pokatron stats", "davew1977@yahoo.co.uk");
                        stats.clear();
                    }
                }
                finally
                {
                    scheduleMail();
                }
            }
        }, 1, TimeUnit.MINUTES);
    }
}
