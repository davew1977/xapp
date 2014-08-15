/*
 *
 * Date: 2011-feb-20
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import net.sf.xapp.net.api.userentity.UserEntity;
import net.sf.xapp.net.api.userentity.UserEntityListener;
import net.sf.xapp.net.api.userentity.UserEntityListenerAdaptor;
import net.sf.xapp.net.common.types.*;

public class UserEntityWrapper extends UserEntity
{
    private final boolean guest;
    private UserEntityListener notifier;
    private boolean bot;
    private boolean online;

    public UserEntityWrapper()
    {
        super();
        guest = false;
    }

    public UserEntityWrapper(UserId userId, UserInfo userInfo, Coord userspaceLocation, boolean guest)
    {
        super(userId, userInfo, userspaceLocation);
        this.guest = guest;
    }

    @Override
    public void addFollowedUser(UserId userId)
    {
        if(getFollowedUsers().contains(userId))
        {
            throw new GenericException(ErrorCode.USER_ALREADY_FOLLOWED);
        }
        super.addFollowedUser(userId);
    }

    public UserId removeFollowedUser(UserId userId)
    {
        int index = getFollowedUsers().indexOf(userId);
        if(index==-1)
        {
            throw new GenericException(ErrorCode.NOT_FOLLOWING_USER);
        }
        return super.removeFollowedUser(index);
    }

    public void setNotifier(UserEntityListenerAdaptor listener)
    {
        if(this.notifier != null) //stored only so that it can removed
        {
            removeListener(this.notifier);
        }
        if (listener!=null)
        {
            this.notifier = listener;
            addListener(listener);
        }
    }

    public boolean isGuest()
    {
        return guest;
    }

    public void setBot(boolean bot)
    {
        this.bot = bot;
    }

    public boolean isBot()
    {
        return bot;
    }

    public boolean isAdmin()
    {
        return getUserId().getValue().equals("0_p_0");
    }

    public void setOnline(boolean online)
    {
        this.online = online;
    }
}
