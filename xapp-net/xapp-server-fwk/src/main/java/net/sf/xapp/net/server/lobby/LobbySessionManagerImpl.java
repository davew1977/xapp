/*
 *
 * Date: 2010-jun-28
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.lobby;

import net.sf.xapp.net.api.clientlobbysession.ClientLobbySession;
import net.sf.xapp.net.api.clientlobbysession.ClientLobbySessionAdaptor;
import net.sf.xapp.net.api.lobbyinternal.LobbyInternal;
import net.sf.xapp.net.api.lobbysessionmanager.LobbySessionManager;
import net.sf.xapp.net.api.lobbysessionmanager.LobbySessionManagerReply;
import net.sf.xapp.net.api.lobbysessionmanager.LobbySessionManagerReplyAdaptor;
import net.sf.xapp.net.common.types.*;
import net.sf.xapp.net.server.channels.AppAdaptor;
import net.sf.xapp.net.server.channels.CommChannel;
import net.sf.xapp.net.server.channels.NotifyProxy;
import net.sf.xapp.net.server.framework.memdb.StorableType;
import net.sf.xapp.net.server.framework.memdb.SubscriptionService;
import net.sf.xapp.net.server.framework.memdb.SubscriptionServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LobbySessionManagerImpl extends AppAdaptor implements LobbySessionManager, LobbyInternal
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SubscriptionService subscriptionService;
    private final String lobbyKey;
    private CommChannel commChannel;
    private LobbySessionManagerReply lobbySessionManagerReply;
    private Map<UserId, Map<Integer, PageHandler>> pageHandlers;

    public LobbySessionManagerImpl(String lobbyKey, StorableType storableType)
    {
        this.subscriptionService = new SubscriptionServiceImpl(storableType, lobbyKey);
        this.lobbyKey = lobbyKey;
        pageHandlers = new HashMap<UserId, Map<Integer, PageHandler>>();
    }

    public void setCommChannel(CommChannel commChannel)
    {
        this.commChannel = commChannel;
        lobbySessionManagerReply = new LobbySessionManagerReplyAdaptor(lobbyKey,
                new NotifyProxy<LobbySessionManagerReply>(commChannel));
    }

    @Override
    public void subscribe(final UserId principal, final Integer viewId, Integer pageSize, QueryData query)
    {
        final ClientLobbySession clientLobbySession = new ClientLobbySessionAdaptor(lobbyKey,
                new NotifyProxy<ClientLobbySession>(commChannel));

        PageHandler pageHandler = new PageHandler(pageSize, clientLobbySession, principal, viewId);
        Set<LobbyEntity> lobbyEntities = subscriptionService.subscribe(query, pageHandler);
        pageHandler.setMatches(new ArrayList<LobbyEntity>(lobbyEntities));

        lobbySessionManagerReply.subscribeResponse(principal, pageHandler.currentPage(), null);

        //deactivate other views
        //deactivatePageHandlers(principal);

        //unsubscribe from the old query associated with this view id
        PageHandler oldPageHandler = pageHandlers(principal).put(viewId, pageHandler);
        if(oldPageHandler!=null)
        {
            subscriptionService.unsubscribe(oldPageHandler);
        }
    }

    @Override
    public void pageUp(UserId principal, Integer viewId)
    {
        PageHandler pageHandler = activePageHandler(principal, viewId);
        lobbySessionManagerReply.pageUpResponse(principal, pageHandler.pageUp(), null);
    }

    @Override
    public void pageDown(UserId principal, Integer viewId)
    {
        PageHandler pageHandler = activePageHandler(principal, viewId);
        lobbySessionManagerReply.pageDownResponse(principal, pageHandler.pageDown(), null);
    }

    @Override
    public void pause(UserId principal)
    {
        deactivatePageHandlers(principal);
    }

    @Override
    public void switchView(UserId principal, Integer viewId)
    {
        for (Map.Entry<Integer, PageHandler> e : pageHandlers(principal).entrySet())
        {
            e.getValue().setActive(e.getKey().equals(viewId));
        }
        lobbySessionManagerReply.switchViewResponse(principal,
                activePageHandler(principal, viewId).currentPage(), null);
    }

    @Override
    public void userDisconnected(UserId userId)
    {
        log.debug("player disconnected from lobby session: " + userId);
        userLeft(userId);
        commChannel.removeUser(userId);
    }

    @Override
    public void userLeft(UserId userId)
    {
        Map<Integer, PageHandler> phmap = pageHandlers.remove(userId);
        if(phmap!=null)
        {
            for (PageHandler pageHandler : phmap.values())
            {
                subscriptionService.unsubscribe(pageHandler);
            }
        }
    }

    private void deactivatePageHandlers(UserId userId)
    {
        for (PageHandler handler : pageHandlers(userId).values())
        {
            handler.setActive(false);
        }
    }

    private PageHandler activePageHandler(UserId principal, Integer viewId)
    {
        PageHandler pageHandler = pageHandlers(principal).get(viewId);
        if(pageHandler==null)
        {
            throw new GenericException(ErrorCode.VIEW_DOES_NOT_EXIST);
        }
        if(!pageHandler.isActive())
        {
            throw new GenericException(ErrorCode.VIEW_NOT_ACTIVE);
        }
        return pageHandler;
    }

    private Map<Integer, PageHandler> pageHandlers(UserId principal)
    {
        Map<Integer, PageHandler> phmap = pageHandlers.get(principal);
        if (phmap == null)
        {
            phmap = new HashMap<Integer, PageHandler>();
            pageHandlers.put(principal, phmap);
        }
        return phmap;
    }

    @Override
    public void entityAdded(String key, LobbyEntity entity)
    {
        subscriptionService.addItem(key, entity);
    }

    @Override
    public void entityRemoved(String key)
    {
        subscriptionService.removeItem(key);
    }

    @Override
    public void propertyChanged(String key, LobbyPropertyEnum property, String value)
    {
        subscriptionService.itemChanged(key, property.name(), value);              
    }

    @Override
    public void listPropertyChanged(String key, LobbyPropertyEnum property, Integer index, String value, ListOp listOp)
    {
        subscriptionService.itemChanged(key, property.name(), index, value, listOp);
    }

    @Override
    public String getKey()
    {
        return lobbyKey;
    }

    @Override
    public AppType getAppType()
    {
        return AppType.LOBBY;
    }

    /**
     *
     * @return the number of items in the lobby
     */
    public int size()
    {
        return subscriptionService.size();
    }
}