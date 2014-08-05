package net.sf.xapp.net.server.playerrepository;

import ngpoker.backend.useradmin.UserAdmin;
import ngpoker.backend.userapi.UserApi;
import ngpoker.backend.userapi.to.LoginAsGuestResponse;
import ngpoker.backend.userapi.to.LoginResponse;
import ngpoker.backend.userapi.to.SignUpResponse;
import ngpoker.common.types.GenericException;
import ngpoker.common.types.ImageData;
import ngpoker.common.types.PlayerId;
import ngpoker.user.UserInfo;

public class UserApiImpl implements UserApi
{
    private final UserStore userStore;

    public UserApiImpl(UserStore userStore)
    {
        this.userStore = userStore;
    }

    @Override
    public void loginWithToken(PlayerId playerId, String token, Boolean bot) throws GenericException {
        userStore.authenticateUser(playerId, token, bot);
    }

    @Override
    public SignUpResponse signUp(UserInfo userInfo, ImageData profileImage)
    {
        PlayerId playerId = userStore.addUser(userInfo, profileImage);
        return new SignUpResponse(playerId, null);
    }

    @Override
    public LoginResponse login(String nickname, String password)
    {
        PlayerId playerId = userStore.authenticateUser(nickname, password);
        return new LoginResponse(playerId, null);
    }

    @Override
    public LoginAsGuestResponse loginAsGuest(String nickname)
    {
        PlayerId playerId = userStore.obtainGuestId(nickname);
        return new LoginAsGuestResponse(playerId, null);
    }

    @Override
    public void resetPassword(String nickname, String emailAddress)
    {
        userStore.resetPassword(nickname, emailAddress);
    }
}
