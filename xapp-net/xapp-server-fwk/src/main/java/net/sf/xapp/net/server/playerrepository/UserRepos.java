/*
 *
 * Date: 2011-feb-06
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import net.sf.xapp.net.api.userlookup.UserLookup;
import net.sf.xapp.net.api.userlookup.to.FindUserResponse;
import net.sf.xapp.net.common.types.GenericException;
import net.sf.xapp.net.common.types.User;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.common.types.UserInfo;

public class UserRepos implements UserLookup
{
    private final UserStore userStore;

    public UserRepos(UserStore userStore)
    {
        this.userStore = userStore;
    }

    @Override
    public FindUserResponse findUser(UserId id) throws GenericException
    {
        UserEntityWrapper user = userStore.getUser(id);
        UserInfo userInfo = user.getUserInfo();
        return new FindUserResponse(new User(id, userInfo.getNickname(),
                user.isGuest(), user.isAdmin(), userInfo.getEmail(),userInfo.getCountry()));
    }
}
