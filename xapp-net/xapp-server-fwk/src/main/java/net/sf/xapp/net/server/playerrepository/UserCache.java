/*
 *
 * Date: 2011-feb-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.GenericException;
import net.sf.xapp.net.common.types.UserId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserCache
{
    private Map<UserId, UserEntityWrapper> mapById;
    private Map<String, UserEntityWrapper> mapByNickname;

    public UserCache()
    {
        mapById = new ConcurrentHashMap<UserId, UserEntityWrapper>();
        mapByNickname = new ConcurrentHashMap<String, UserEntityWrapper>();
    }

    public UserEntityWrapper getByNickname(String nickname)
    {
        UserEntityWrapper user = mapByNickname.get(nickname);
        if (user == null)
        {
            throw new GenericException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public UserEntityWrapper getById(UserId userId)
    {
        return mapById.get(userId);
    }

    public void addUser(UserEntityWrapper user)
    {
        mapById.put(user.getUserId(), user);
        mapByNickname.put(user.getUserInfo().getNickname(), user);
    }

    public boolean isNicknameUsed(String nickname)
    {
        return mapByNickname.containsKey(nickname);
    }

    public void removeUser(UserEntityWrapper user)
    {
        mapById.remove(user.getUserId());
        mapByNickname.remove(user.getUserInfo().getNickname());
    }
}
