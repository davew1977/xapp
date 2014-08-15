/*
 *
 * Date: 2011-feb-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import net.sf.xapp.net.common.types.ImageData;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.common.types.UserInfo;

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

    UserEntityWrapper getUser(UserId userId);
    UserId getUserId(String nickname);
}
