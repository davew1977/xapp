package net.sf.xapp.net.server.playerrepository;

import net.sf.xapp.net.api.userapi.UserApi;
import net.sf.xapp.net.api.userapi.to.LoginAsGuestResponse;
import net.sf.xapp.net.api.userapi.to.LoginResponse;
import net.sf.xapp.net.api.userapi.to.SignUpResponse;
import net.sf.xapp.net.common.types.GenericException;
import net.sf.xapp.net.common.types.ImageData;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.common.types.UserInfo;

public class UserApiImpl implements UserApi
{
    private final UserStore userStore;

    public UserApiImpl(UserStore userStore)
    {
        this.userStore = userStore;
    }

    @Override
    public void loginWithToken(UserId userId, String token, Boolean bot) throws GenericException {
        userStore.authenticateUser(userId, token, bot);
    }

    @Override
    public SignUpResponse signUp(UserInfo userInfo, ImageData profileImage)
    {
        UserId userId = userStore.addUser(userInfo, profileImage);
        return new SignUpResponse(userId, null);
    }

    @Override
    public LoginResponse login(String nickname, String password)
    {
        UserId userId = userStore.authenticateUser(nickname, password);
        return new LoginResponse(userId, null);
    }

    @Override
    public LoginAsGuestResponse loginAsGuest(String nickname)
    {
        UserId userId = userStore.obtainGuestId(nickname);
        return new LoginAsGuestResponse(userId, null);
    }

    @Override
    public void resetPassword(String nickname, String emailAddress)
    {
        userStore.resetPassword(nickname, emailAddress);
    }
}
