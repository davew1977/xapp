/*
 *
 * Date: 2011-feb-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import net.sf.xapp.net.api.clientcontrol.ClientControl;
import net.sf.xapp.net.api.clientcontrol.ClientControlAdaptor;
import net.sf.xapp.net.api.connectionlistener.ConnectionListener;
import net.sf.xapp.net.api.lobbyinternal.LobbyInternal;
import net.sf.xapp.net.api.messagesender.MessageSender;
import net.sf.xapp.net.api.useradmin.UserAdmin;
import net.sf.xapp.net.api.useradmin.UserAdminReply;
import net.sf.xapp.net.api.useradmin.UserAdminReplyAdaptor;
import net.sf.xapp.net.api.userentity.UserEntity;
import net.sf.xapp.net.api.userentity.UserEntityListener;
import net.sf.xapp.net.api.userentity.UserEntityListenerAdaptor;
import net.sf.xapp.net.common.types.*;
import net.sf.xapp.net.common.util.random.RandomEntropySource;
import net.sf.xapp.net.server.idgen.IdSeqGenerator;
import net.sf.xapp.net.server.channels.UserLocator;
import net.sf.xapp.net.server.channels.SimpleNotifier;
import net.sf.xapp.net.server.framework.email.MailProxy;
import net.sf.xapp.net.server.framework.persistendb.FileDB;
import net.sf.xapp.net.server.util.PasswordGenerator;
import org.slf4j.Logger;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.sf.xapp.net.common.types.LobbyPropertyEnum.*;


/**
 * UserEntityWrapper store that stores user data on file
 */
public class SimpleUserStore implements UserStore, ConnectionListener, UserAdmin
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MessageSender messageSender;
    private final AtomicInteger guestUserIdSeq = new AtomicInteger(0);
    private final UserCache userCache;
    private final ClientControl clientControl;
    private final FileDB<UserEntityWrapper, UserEntityListener> userDB;
    private final LobbyInternal lobbyInternal;
    private final UserAdminReply userAdminReply;
    private final ImageCache imageCache;
    private final UserLocator userLocator;
    private final BasicPasswordEncryptor passwordEncryptor;

    private final IdSeqGenerator idSeqGenerator;
    private final MailProxy mailProxy;
    private UserspaceCoordDeck userspaceCoordDeck;
    private UserspaceCoordDeck guestCoordDeck;

    public SimpleUserStore(FileDB<UserEntityWrapper, UserEntityListener> userDB,
                           MessageSender messageSender,
                           IdSeqGenerator idSeqGenerator,
                           String imageDir,
                           LobbyInternal lobbyInternal,
                           UserLocator userLocator, MailProxy mailProxy) throws URISyntaxException
    {
        this.userDB = userDB;
        this.messageSender = messageSender;
        this.idSeqGenerator = idSeqGenerator;
        this.lobbyInternal = lobbyInternal;
        this.userLocator = userLocator;
        this.mailProxy = mailProxy;
        this.passwordEncryptor = new BasicPasswordEncryptor();
        userCache = new UserCache();
        clientControl = new ClientControlAdaptor(new SimpleNotifier<ClientControl>(messageSender));
        userAdminReply = new UserAdminReplyAdaptor(new SimpleNotifier<UserAdminReply>(messageSender));

        imageCache = new ImageCacheImpl(imageDir);
    }

    public void init()
    {
        List<UserEntityWrapper> users = userDB.readAll();
        userspaceCoordDeck = new UserspaceCoordDeck(new RandomEntropySource(), 100, users.size(), 100);
        guestCoordDeck = new UserspaceCoordDeck(new RandomEntropySource(), 100, 1000, 1000);
        for (UserEntityWrapper user : users)
        {
            userCache.addUser(user);
            String pid = user.getUserId().getValue();
            user.addListener(new UserEntityListenerAdaptor(pid, userDB));
            user.addListener(new UserLobbyListener(user));
            userspaceCoordDeck.use(user.getUserspaceLocation());
            log.debug("adding user from backup: " + user);
            addUserToLobby(user);
        }
        log.info("loaded " + users.size() + " users into memory");
        log.info("player id seq starting at " + idSeqGenerator.peek(EntityType.player));
    }

    @Override
    public synchronized UserId addUser(UserInfo userInfo, ImageData profileImage)
    {
        //check nickname unique
        String nickname = userInfo.getNickname();

        checkNicknameUnique(nickname);
        //TODO check valid email
        //TODO check email unique? - difficult without some index on email - what if the user info are sharded on multi nodes?
        //TODO validate other

        UserId userId = new UserId(idSeqGenerator.nextId(EntityType.player));

        //encrypt password
        userInfo.setPassword(passwordEncryptor.encryptPassword(userInfo.getPassword()));

        Coord userspaceLocation = userspaceCoordDeck.draw();
        final UserEntityWrapper user = createUser(userId, userInfo, userspaceLocation, false);
        String pid = userId.getValue();
        user.addListener(new UserEntityListenerAdaptor(pid, userDB));
        user.addListener(new UserLobbyListener(user));
        userDB.add(pid, user);
        userCache.addUser(user);
        if(profileImage!=null)
        {
            imageCache.save(userId, profileImage);
        }

        addUserToLobby(user);
        return userId;
    }

    private void addUserToLobby(UserEntityWrapper user)
    {
        String pid = user.getUserId().getValue();
        int x = user.getUserspaceLocation().getX();
        int y = user.getUserspaceLocation().getY();
        String nickname = user.getUserInfo().getNickname();
        Country country = user.getUserInfo().getCountry();
        UserInLobby userInLobby = new UserInLobby(x, y, country, pid, nickname);
        userInLobby.setStatus(user.getStatus());
        userInLobby.setLastLoginTime(user.getLastLoginTime());
        userInLobby.setPoints(user.getPlayerPoints());
        lobbyInternal.entityAdded(pid, userInLobby);
    }

    public static UserEntityWrapper createUser(UserId userId, UserInfo userInfo,
                                  Coord userspaceLocation, boolean guest)
    {
        return new UserEntityWrapper(userId, userInfo, userspaceLocation, guest);
    }

    private synchronized void checkNicknameUnique(String nickname)
    {
        if (userCache.isNicknameUsed(nickname))
        {
            throw new GenericException(ErrorCode.NICKNAME_NOT_UNIQUE);
        }
    }


    @Override
    public synchronized UserId authenticateUser(String nickname, String password)
    {
        UserEntity user = userCache.getByNickname(nickname);
        authenticateInternal(password, user);
        return user.getUserId();
    }

    /**
     * this is the method that will definitely be called regardless of the login mechanism (logging in as guest,
     * token login, sign up, or username/password login)
     * @param userId
     * @param keyToken
     * @param bot
     * @return
     */
    @Override
    public synchronized UserId authenticateUser(UserId userId, String keyToken, boolean bot)
    {
        UserEntityWrapper user = getUser(userId);
        //TODO check token
        user.setLastLoginTime(System.currentTimeMillis());
        user.setBot(bot);
        return user.getUserId();
    }

    private void authenticateInternal(String password, UserEntity user)
    {
        if (!passwordEncryptor.checkPassword(password, user.getUserInfo().getPassword()))
        {
            throw new GenericException(ErrorCode.INVALID_PASSWORD);
        }
    }

    @Override
    public synchronized UserId obtainGuestId(String nickname)
    {
        checkNicknameUnique(nickname);
        UserId userId = new UserId(String.valueOf(guestUserIdSeq.decrementAndGet()));
        Coord coord = guestCoordDeck.draw();
        UserEntityWrapper guest = createUser(userId,
                new UserInfo("","","",nickname, Country.Zimbabwe, ""), coord, true);
        userCache.addUser(guest);
        guest.addListener(new UserLobbyListener(guest));

        addUserToLobby(guest);
        return userId;
    }

    @Override
    public UserId getUserId(String nickname)
    {
        return userCache.getByNickname(nickname).getUserId();

    }

    @Override
    public synchronized void updatePoints(UserId principal, Long newValue)
    {
        UserEntityWrapper user = getUser(principal);
        user.setPlayerPoints(newValue);
    }

    @Override
    public synchronized void addPoints(UserId principal, Long delta)
    {
        UserEntityWrapper user = getUser(principal);
        user.setPlayerPoints(user.getPlayerPoints() + delta);
    }

    @Override
    public void userConnected(UserId userId, NodeId nodeId)
    {
        UserEntityWrapper user = getUser(userId);

        UserEntityListenerAdaptor listener = new UserEntityListenerAdaptor(userId.getValue(),
                new SimpleNotifier<UserEntityListener>(messageSender, userId));
        user.setNotifier(listener);
        user.setOnline(true);

        clientControl.setUser(userId, user);
        lobbyInternal.propertyChanged(userId.getValue(), online, "true");
    }

    @Override
    public void userDisconnected(UserId userId)
    {
        UserEntityWrapper user = getUser(userId);
        user.setNotifier(null);
        if (user.isGuest())
        {
            userCache.removeUser(user);
            lobbyInternal.entityRemoved(userId.getValue());

        }
        else
        {
            lobbyInternal.propertyChanged(userId.getValue(), online, "false");
        }
        user.setOnline(false);

    }

    @Override
    public UserEntityWrapper getUser(UserId userId)
    {
        UserEntityWrapper user = userCache.getById(userId);
        if(user==null)
        {
            throw new GenericException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public void updateImage(UserId principal, ImageData profileImage)
    {
        UserEntityWrapper user = getUser(principal);
        user.setLastImageUploadTime(System.currentTimeMillis());
        imageCache.save(principal, profileImage);
    }

    @Override
    public void updateStatus(UserId principal, String status)
    {
        getUser(principal).setStatus(status);
    }

    @Override
    public void updatePassword(UserId principal, String oldPass, String newPass)
    {
        UserEntityWrapper user = getUser(principal);
        authenticateInternal(oldPass, user);
        encryptAndSet(user, newPass);
    }

    private void encryptAndSet(UserEntityWrapper user, String newPass)
    {
        String encrypted = passwordEncryptor.encryptPassword(newPass);
        user.setUserInfoPassword(encrypted);
    }

    public void resetPassword(String nickname, String emailAddress)
    {
        UserEntityWrapper user = userCache.getByNickname(nickname);
        if(!user.getUserInfo().getEmail().equals(emailAddress))
        {
            throw new GenericException(ErrorCode.INCORRECT_EMAIL);
        }
        //generate new password
        String password = PasswordGenerator.generatePassword();
        encryptAndSet(user, password);
        mailProxy.sendMail("your password has been reset to \"" + password + "\"",
                "reset password", user.getUserInfo().getEmail());

    }

    @Override
    public void getImage(UserId principal, UserId userId)
    {
        ImageData image = imageCache.load(userId);
        userAdminReply.getImageResponse(principal, userId, image, null);
    }

    @Override
    public void getUserDetails(UserId principal, UserId userId)
    {
        UserEntityWrapper user = getUser(userId);
        List<UserLocation> locations = userLocator.getLocations(userId, AppType.OBJ_SERVER);
        userAdminReply.getUserDetailsResponse(principal, userId, locations, user.getFollowedUsers(), null);
    }

    private class UserLobbyListener extends UserEntityListenerAdaptor
    {
        final UserEntityWrapper user;

        public UserLobbyListener(UserEntityWrapper user)
        {
            super(user.getUserId().getValue());
            this.user = user;
        }

        @Override
        public void lastLoginTimeChanged(Long oldValue, Long newValue)
        {
            lobbyInternal.propertyChanged(key, lastLoginTime, newValue + "");
        }

        @Override
        public void statusChanged(String oldValue, String newValue)
        {
            lobbyInternal.propertyChanged(key, status, newValue);
        }

        @Override
        public void lastImageUploadTimeChanged(Long oldValue, Long newValue)
        {
            lobbyInternal.propertyChanged(key, lastImageUploadTime, newValue + "");
        }

    }
}
