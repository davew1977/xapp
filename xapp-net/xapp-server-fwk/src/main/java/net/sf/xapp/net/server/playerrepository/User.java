/*
 *
 * Date: 2011-feb-20
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import ngpoker.common.types.*;
import ngpoker.moneysystem.types.Account;
import ngpoker.user.UserEntity;
import ngpoker.user.UserEntityListener;
import ngpoker.user.UserEntityListenerAdaptor;
import ngpoker.user.UserInfo;

public class User extends UserEntity
{
    private final boolean guest;
    private UserEntityListener notifier;
    private boolean bot;
    private boolean online;

    public User()
    {
        super();
        guest = false;
    }

    public User(PlayerId playerId, UserInfo userInfo, Coord userspaceLocation, boolean guest)
    {
        super(playerId, userInfo, userspaceLocation);
        this.guest = guest;
    }

    public Account getAccount(AccountType accountType)
    {
        for (Account account : getAccounts())
        {
            if(account.getAccountType().equals(accountType))
            {
                return account;
            }
        }
        return null;
    }

    public void setAccountBalance(Account account, long balance)
    {
        setAccountBalance(getAccounts().indexOf(account), balance);
    }

    @Override
    public void addFollowedUser(PlayerId playerId)
    {
        if(getFollowedUsers().contains(playerId))
        {
            throw new GenericException(ErrorCode.USER_ALREADY_FOLLOWED);
        }
        super.addFollowedUser(playerId);
    }

    public PlayerId removeFollowedUser(PlayerId playerId)
    {
        int index = getFollowedUsers().indexOf(playerId);
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
        return getPlayerId().getValue().equals("0_p_0");
    }

    public void setOnline(boolean online)
    {
        this.online = online;
    }
}
