/*
 *
 * Date: 2011-feb-06
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import ngpoker.common.types.GenericException;
import ngpoker.common.types.OperatorId;
import ngpoker.common.types.Player;
import ngpoker.common.types.PlayerId;
import ngpoker.playerlookup.PlayerLookup;
import ngpoker.playerlookup.to.FindPlayerResponse;
import ngpoker.user.UserInfo;

public class PlayerRepos implements PlayerLookup
{
    private final UserStore userStore;

    public PlayerRepos(UserStore userStore)
    {
        this.userStore = userStore;
    }

    @Override
    public FindPlayerResponse findPlayer(PlayerId id) throws GenericException
    {
        User user = userStore.getUser(id);
        UserInfo userInfo = user.getUserInfo();
        return new FindPlayerResponse(new Player(id, userInfo.getNickname(),
                new OperatorId(100), userInfo.getCountry(), user.isGuest(), user.isAdmin(), userInfo.getEmail()));
    }
}
