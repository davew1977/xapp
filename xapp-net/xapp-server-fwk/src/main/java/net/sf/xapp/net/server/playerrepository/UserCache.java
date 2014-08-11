/*
 *
 * Date: 2011-feb-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import ngpoker.common.types.GenericException;
import ngpoker.common.types.ErrorCode;
import net.sf.xapp.net.common.types.UserId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserCache
{
    private Map<UserId, User> mapById;
    private Map<String, User> mapByNickname;

    public UserCache()
    {
        mapById = new ConcurrentHashMap<UserId, User>();
        mapByNickname = new ConcurrentHashMap<String, User>();
    }

    public User getByNickname(String nickname)
    {
        User user = mapByNickname.get(nickname);
        if (user == null)
        {
            throw new GenericException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public User getById(UserId userId)
    {
        return mapById.get(userId);
    }

    public void addUser(User user)
    {
        mapById.put(user.getUserId(), user);
        mapByNickname.put(user.getUserInfo().getNickname(), user);
    }

    public boolean isNicknameUsed(String nickname)
    {
        return mapByNickname.containsKey(nickname);
    }

    public void removeUser(User user)
    {
        mapById.remove(user.getUserId());
        mapByNickname.remove(user.getUserInfo().getNickname());
    }
}
