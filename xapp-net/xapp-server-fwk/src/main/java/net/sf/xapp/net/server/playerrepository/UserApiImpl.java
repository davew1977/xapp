package net.sf.xapp.net.server.playerrepository;

import ngpoker.backend.useradmin.UserAdmin;
import ngpoker.backend.userapi.UserApi;
import ngpoker.backend.userapi.to.LoginAsGuestResponse;
import ngpoker.backend.userapi.to.LoginResponse;
import ngpoker.backend.userapi.to.SignUpResponse;
import ngpoker.common.types.GenericException;
import ngpoker.common.types.ImageData;
import net.sf.xapp.net.common.types.UserId;
import ngpoker.user.UserInfo;

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
