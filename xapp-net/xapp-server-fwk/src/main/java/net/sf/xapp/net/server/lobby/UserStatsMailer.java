package net.sf.xapp.net.server.lobby;

import net.sf.xapp.net.server.clustering.ClusterFacade;
import ngpoker.common.types.Country;
import ngpoker.common.types.ListOp;
import ngpoker.common.types.LobbyPropertyEnum;
import ngpoker.common.types.Player;
import ngpoker.common.types.PlayerId;
import net.sf.xapp.net.server.lobby.internal.LobbyInternal;
import net.sf.xapp.net.server.lobby.types.LobbyEntity;
import ngpoker.playerlookup.PlayerLookup;
import net.sf.xapp.net.server.framework.email.MailProxy;
import net.sf.xapp.net.server.framework.eventloop.EventLoopManager;
import net.sf.xapp.net.server.framework.eventloop.EventLoopMessageHandler;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class UserStatsMailer implements LobbyInternal
{
    private final Logger log = Logger.getLogger(getClass());
    private final MailProxy mailProxy;
    private final ScheduledExecutorService scheduledExecutorService;
    private final PlayerLookup playerLookup;
    private final List<String> stats;
    private final Set<String> excludedPids;

    public UserStatsMailer(String lobbyName,
                           MailProxy mailProxy,
                           ClusterFacade clusterFacade,
                           EventLoopManager eventLoopManager,
                           ScheduledExecutorService scheduledExecutorService,
                           PlayerLookup playerLookup)
    {
        this.mailProxy = mailProxy;
        this.playerLookup = playerLookup;
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
        Player player = playerLookup.findPlayer(new PlayerId(entityKey)).getPlayer();
        String nickname = player.getUsername();
        Country country = player.getCountry();
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
