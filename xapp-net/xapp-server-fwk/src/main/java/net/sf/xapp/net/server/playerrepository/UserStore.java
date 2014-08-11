/*
 *
 * Date: 2011-feb-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import ngpoker.common.types.ImageData;
import net.sf.xapp.net.common.types.UserId;
import ngpoker.user.UserInfo;

/**
 *
 */
public interface UserStore
{
    UserId addUser(UserInfo userInfo, ImageData profileImage);
    UserId authenticateUser(String nickname, String password);
    UserId authenticateUser(UserId userId, String keyToken, boolean bot);
    UserId obtainGuestId(String nickname);

    void resetPassword(String nickname, String email);

    User getUser(UserId userId);
    UserId getUserId(String nickname);
}
