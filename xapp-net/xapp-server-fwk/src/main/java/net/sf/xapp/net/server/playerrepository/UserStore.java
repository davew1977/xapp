/*
 *
 * Date: 2011-feb-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import ngpoker.common.types.ImageData;
import ngpoker.common.types.PlayerId;
import ngpoker.user.UserInfo;

/**
 *
 */
public interface UserStore
{
    PlayerId addUser(UserInfo userInfo, ImageData profileImage);
    PlayerId authenticateUser(String nickname, String password);
    PlayerId authenticateUser(PlayerId playerId, String keyToken, boolean bot);
    PlayerId obtainGuestId(String nickname);

    void resetPassword(String nickname, String email);

    User getUser(PlayerId playerId);
    PlayerId getPlayerId(String nickname);
}
