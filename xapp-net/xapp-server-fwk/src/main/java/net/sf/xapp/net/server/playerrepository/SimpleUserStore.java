/*
 *
 * Date: 2011-feb-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import net.sf.xapp.net.server.idgen.IdSeqGenerator;
import ngpoker.backend.useradmin.UserAdmin;
import ngpoker.backend.useradmin.UserAdminReply;
import ngpoker.backend.useradmin.UserAdminReplyAdaptor;
import net.sf.xapp.net.server.channels.PlayerLocator;
import net.sf.xapp.net.server.channels.SimpleNotifier;
import ngpoker.common.types.*;
import net.sf.xapp.net.server.connectionserver.clientcontrol.ClientControl;
import net.sf.xapp.net.server.connectionserver.clientcontrol.ClientControlAdaptor;
import net.sf.xapp.net.server.connectionserver.listener.ConnectionListener;
import net.sf.xapp.net.server.connectionserver.messagesender.MessageSender;
import ngpoker.infrastructure.types.EntityType;
import ngpoker.infrastructure.types.NodeId;
import ngpoker.limiteddeck.PokerCharacter;
import net.sf.xapp.net.server.lobby.internal.LobbyInternal;
import net.sf.xapp.net.server.lobby.types.PlayerInLobby;
import ngpoker.moneysystem.backend.BackendMoney;
import ngpoker.moneysystem.backend.to.GetAccountBalanceResponse;
import ngpoker.moneysystem.types.Account;
import net.sf.xapp.net.server.framework.email.MailProxy;
import net.sf.xapp.net.server.framework.persistendb.FileDB;
import net.sf.xapp.net.server.util.PasswordGenerator;
import ngpoker.common.util.deck.RandomEntropySource;
import ngpoker.user.*;
import org.apache.log4j.Logger;
import org.jasypt.util.password.BasicPasswordEncryptor;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static ngpoker.common.types.LobbyPropertyEnum.*;

/**
 * User store that stores user data on file
 */
public class SimpleUserStore implements UserStore, BackendMoney, ConnectionListener, UserAdmin
{
    private final Logger log = Logger.getLogger(getClass());
    private final MessageSender messageSender;
    private final AtomicInteger guestUserIdSeq = new AtomicInteger(0);
    private final UserCache userCache;
    private final ClientControl clientControl;
    private final FileDB<User, UserEntityListener> userDB;
    private final LobbyInternal lobbyInternal;
    private final UserAdminReply userAdminReply;
    private final ImageCache imageCache;
    private final PlayerLocator playerLocator;
    private final BasicPasswordEncryptor passwordEncryptor;

    private final IdSeqGenerator idSeqGenerator;
    private final MailProxy mailProxy;
    private UserspaceCoordDeck userspaceCoordDeck;
    private UserspaceCoordDeck guestCoordDeck;

    public SimpleUserStore(FileDB<User, UserEntityListener> userDB,
                           MessageSender messageSender,
                           IdSeqGenerator idSeqGenerator,
                           String imageDir,
                           LobbyInternal lobbyInternal,
                           PlayerLocator playerLocator, MailProxy mailProxy) throws URISyntaxException
    {
        this.userDB = userDB;
        this.messageSender = messageSender;
        this.idSeqGenerator = idSeqGenerator;
        this.lobbyInternal = lobbyInternal;
        this.playerLocator = playerLocator;
        this.mailProxy = mailProxy;
        this.passwordEncryptor = new BasicPasswordEncryptor();
        userCache = new UserCache();
        clientControl = new ClientControlAdaptor(new SimpleNotifier<ClientControl>(messageSender));
        userAdminReply = new UserAdminReplyAdaptor(new SimpleNotifier<UserAdminReply>(messageSender));

        imageCache = new ImageCacheImpl(imageDir);
    }

    public void init()
    {
        List<User> users = userDB.readAll();
        userspaceCoordDeck = new UserspaceCoordDeck(new RandomEntropySource(), 100, users.size(), 100);
        guestCoordDeck = new UserspaceCoordDeck(new RandomEntropySource(), 100, 1000, 1000);
        for (User user : users)
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
        final User user = createUser(userId, userInfo, 100000,100000, userspaceLocation, false);
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

    private void addUserToLobby(User user)
    {
        String pid = user.getUserId().getValue();
        int x = user.getUserspaceLocation().getX();
        int y = user.getUserspaceLocation().getY();
        String nickname = user.getUserInfo().getNickname();
        Country country = user.getUserInfo().getCountry();
        PlayerInLobby playerInLobby = new PlayerInLobby(x, y, country, pid, nickname);
        playerInLobby.setStatus(user.getStatus());
        playerInLobby.setLastLoginTime(user.getLastLoginTime());
        playerInLobby.setPoints(user.getPlayerPoints());
        lobbyInternal.entityAdded(pid, playerInLobby);
    }

    public static User createUser(UserId userId, UserInfo userInfo, long realBalance, long playBalance,
                                  Coord userspaceLocation, boolean guest)
    {
        User user = new User(userId, userInfo, userspaceLocation, guest);
        Account playAccount = new Account(new AccountType(MoneyType.PLAY_MONEY, "USD"));
        Account realAccount = new Account(new AccountType(MoneyType.REAL_MONEY, "USD"));
        playAccount.setBalance(playBalance);
        realAccount.setBalance(realBalance);
        user.getAccounts().add(playAccount);
        user.getAccounts().add(realAccount);
        return user;
    }

    private synchronized void checkNicknameUnique(String nickname)
    {
        if (userCache.isNicknameUsed(nickname))
        {
            throw new GenericException(ErrorCode.NICKNAME_NOT_UNIQUE);
        }
    }

    @Override
    public synchronized void isBot(UserId principal, UserId userId)
    {
        User user = getUser(userId);
        userAdminReply.isBotResponse(principal, userId, user.isBot(), null);
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
        User user = getUser(userId);
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
        User guest = createUser(userId,
                new UserInfo("","","",nickname, Country.Zimbabwe, ""), 0,1000, coord, true);
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
    public synchronized GetAccountBalanceResponse getAccountBalance(UserId userId, AccountType accountType) throws GenericException
    {
        User user = getUser(userId);
        Account account = getAccount(accountType, user);
        return new GetAccountBalanceResponse(account.getBalance());
    }

    @Override
    public synchronized void debitPlayer(UserId userId, AccountType accountType, Long amount) throws GenericException
    {
        User user = getUser(userId);
        Account account = getAccount(accountType, user);
        if(account.getBalance()<amount)
        {
            throw new GenericException(ErrorCode.NOT_ENOUGH_MONEY);
        }
        user.setAccountBalance(account, account.getBalance()-amount);
    }

    @Override
    public synchronized void setAccountBalance(UserId userId, AccountType accountType, Long amount) throws GenericException
    {
        if(amount<0)
        {
            throw new IllegalArgumentException("amount must be positive");
        }
        User user = getUser(userId);
        Account account = getAccount(accountType, user);
        user.setAccountBalance(account, amount);
    }

    @Override
    public synchronized void updatePoints(UserId principal, Long newValue)
    {
        User user = getUser(principal);
        user.setPlayerPoints(newValue);
    }

    @Override
    public synchronized void addPoints(UserId principal, Long delta)
    {
        User user = getUser(principal);
        user.setPlayerPoints(user.getPlayerPoints() + delta);
    }

    @Override
    public synchronized void updateCharacters(UserId principal, List<PokerCharacter> newValue)
    {
        User user = getUser(principal);
        user.clearEnabledCharacters();
        user.addEnabledCharacters(newValue);
    }

    @Override
    public synchronized void creditPlayer(UserId userId, AccountType accountType, Long amount) throws GenericException
    {
        User user = getUser(userId);
        Account account = getAccount(accountType, user);
        user.setAccountBalance(account, account.getBalance() + amount);
    }

    @Override
    public synchronized void creditOperator(OperatorId operatorId, AccountType accountType, Long amount) throws GenericException
    {
        //TODO
    }

    @Override
    public void playerConnected(UserId userId, NodeId nodeId)
    {
        User user = getUser(userId);

        UserEntityListenerAdaptor listener = new UserEntityListenerAdaptor(userId.getValue(),
                new SimpleNotifier<UserEntityListener>(messageSender, userId));
        user.setNotifier(listener);
        user.setOnline(true);

        clientControl.setUser(userId, user);
        lobbyInternal.propertyChanged(userId.getValue(), LobbyPropertyEnum.online, "true");
    }

    @Override
    public void playerDisconnected(UserId userId)
    {
        User user = getUser(userId);
        user.setNotifier(null);
        if (user.isGuest())
        {
            userCache.removeUser(user);
            lobbyInternal.entityRemoved(userId.getValue());

        }
        else
        {
            lobbyInternal.propertyChanged(userId.getValue(), LobbyPropertyEnum.online, "false");
        }
        user.setOnline(false);

    }

    private Account getAccount(AccountType accountType, User user)
    {
        Account account = user.getAccount(accountType);
        if(account==null)
        {
            throw new GenericException(ErrorCode.ACCOUNT_DOES_NOT_EXIST);
        }
        return account;
    }

    @Override
    public User getUser(UserId userId)
    {
        User user = userCache.getById(userId);
        if(user==null)
        {
            throw new GenericException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public void updateImage(UserId principal, ImageData profileImage)
    {
        User user = getUser(principal);
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
        User user = getUser(principal);
        authenticateInternal(oldPass, user);
        encryptAndSet(user, newPass);
    }

    private void encryptAndSet(User user, String newPass)
    {
        String encrypted = passwordEncryptor.encryptPassword(newPass);
        user.setUserInfoPassword(encrypted);
    }

    public void resetPassword(String nickname, String emailAddress)
    {
        User user = userCache.getByNickname(nickname);
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
    public void getPlayerDetails(UserId principal, UserId userId)
    {
        User user = getUser(userId);
        List<PlayerLocation> locations = playerLocator.getLocations(userId, AppType.TOUR, AppType.CASH_GAME);
        userAdminReply.getPlayerDetailsResponse(principal, userId, locations, user.getFollowedUsers(), null);
    }

    @Override
    public void addUserToFollow(UserId principal, UserId userId)
    {
        if(principal.equals(userId))
        {
            throw new GenericException(ErrorCode.CANNOT_FOLLOW_SELF);
        }
        if(getUser(userId).isGuest())
        {
            throw new GenericException(ErrorCode.CANNOT_FOLLOW_GUEST);
        }
        User user = getUser(principal);
        user.addFollowedUser(userId);
    }

    @Override
    public void removeUserToFollow(UserId principal, UserId userId)
    {
        User user = getUser(principal);
        user.removeFollowedUser(userId);
    }

    private class UserLobbyListener extends UserEntityListenerAdaptor
    {
        final User user;

        public UserLobbyListener(User user)
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

        @Override
        public void playerPointsChanged(Long oldValue, Long newValue)
        {
            lobbyInternal.propertyChanged(key, points, newValue + "");
        }

        @Override
        public void enabledCharacterAdded(PokerCharacter pokerCharacter)
        {
            updateEnabledCharacters();
        }

        @Override
        public void enabledCharactersAdded(List<PokerCharacter> pokerCharacters)
        {
            updateEnabledCharacters();
        }

        @Override
        public void enabledCharacterRemoved(Integer enabledCharacterIndex, PokerCharacter removed)
        {
            updateEnabledCharacters();
        }

        @Override
        public void enabledCharactersCleared()
        {
            updateEnabledCharacters();
        }

        private void updateEnabledCharacters()
        {
            StringBuilder sb = new StringBuilder();
            for (PokerCharacter character : user.getEnabledCharacters())
            {
                sb.append(character).append(",");
            }
            lobbyInternal.propertyChanged(key, enabledCharacters, sb.toString());
        }
    }
}
