/*
 *
 * Date: 2010-sep-22
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.lobby;

import ngpoker.common.types.GenericException;
import ngpoker.common.types.ListOp;
import ngpoker.common.types.ErrorCode;
import ngpoker.common.types.PlayerId;
import net.sf.xapp.net.server.lobby.clientlobbysession.ClientLobbySession;
import net.sf.xapp.net.server.lobby.types.LobbyEntity;
import net.sf.xapp.net.server.lobby.types.Page;
import net.sf.xapp.net.server.framework.memdb.LiveQueryListener;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class PageHandler implements LiveQueryListener<LobbyEntity>
{
    private final Logger log = Logger.getLogger(getClass());
    private final PlayerId playerId;
    private final int viewId;
    private final int pageSize;
    private final ClientLobbySession clientLobbySession;
    private boolean active;
    private int currentPageIndex;
    private List<LobbyEntity> matches;

    public PageHandler(int pageSize, ClientLobbySession clientLobbySession, PlayerId playerId, int viewId)
    {
        this.pageSize = pageSize;
        this.playerId = playerId;
        this.viewId = viewId;
        this.currentPageIndex = 0;
        this.clientLobbySession = clientLobbySession;
        this.active = true;
    }

    public void setMatches(List<LobbyEntity> matches)
    {
        this.matches = matches;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    @Override
    public void itemAdded(LobbyEntity item)
    {
        matches.add(item);
        if(active && inCurrentPage(item))
        {
            clientLobbySession.entityAdded(playerId, viewId, item);
        }
    }

    @Override
    public void itemRemoved(LobbyEntity item)
    {
        if(active && inCurrentPage(item))
        {
            clientLobbySession.entityRemoved(playerId, viewId, item.getKey());
        }
        matches.remove(item);
    }

    @Override
    public void itemChanged(LobbyEntity item, String propName, String value)
    {
        log.debug(String.format("%s %s %s", viewId, propName, value));
        if(active && inCurrentPage(item))
        {
            clientLobbySession.propertyChanged(playerId, viewId, item.getKey(), propName, value);
        }
    }

    @Override
    public void itemChanged(LobbyEntity item, String propName, int index, String value,  ListOp listOp)
    {
        if (log.isDebugEnabled())
        {
            log.debug(String.format("%s %s %s %s", viewId, propName, index, value));
        }
        if(active && inCurrentPage(item))
        {
            clientLobbySession.listPropertyChanged(playerId, viewId, item.getKey(), propName, value, index, listOp);
        }
    }

    private boolean inCurrentPage(LobbyEntity item)
    {
        int index = matches.indexOf(item);
        boolean before = index < pageStartIndex();
        boolean after = index > pageEndIndex();
        return !(before || after);
    }

    private int pageEndIndex()
    {
        return Math.min(pageSize * (currentPageIndex + 1), matches.size());
    }

    private int pageStartIndex()
    {
        return pageSize * currentPageIndex;
    }

    public Page currentPage()
    {
        List<LobbyEntity> result = new ArrayList<LobbyEntity>();
        for (int i = pageStartIndex(); i < pageEndIndex(); i++)
        {
            result.add(matches.get(i));
        }
        return new Page(result, pageStartIndex(), matches.size(), viewId);
    }

    public Page pageDown()
    {
        if(pageEndIndex()>=matches.size())
        {
            throw new GenericException(ErrorCode.CANNOT_PAGE_DOWN);
        }
        currentPageIndex++;
        return currentPage();
    }

    public Page pageUp()
    {
        if(currentPageIndex ==0)
        {
            throw new GenericException(ErrorCode.CANNOT_PAGE_UP);
        }
        currentPageIndex--;
        return currentPage();
    }

    public boolean isActive()
    {
        return active;
    }

    @Override
    public String toString()
    {
        return "PageHandler{" +
                "playerId=" + playerId +
                ", viewId=" + viewId +
                ", pageSize=" + pageSize +
                ", clientLobbySession=" + clientLobbySession +
                ", active=" + active +
                ", currentPageIndex=" + currentPageIndex +
                ", matches=" + matches +
                '}';
    }
}
