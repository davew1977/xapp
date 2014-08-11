package net.sf.xapp.net.server.minitools;

import net.sf.xapp.net.server.clustering.NodeInfo;
import net.sf.xapp.net.server.clustering.NodeInfoImpl;
import ngpoker.common.types.AccountType;
import ngpoker.common.types.Country;
import ngpoker.common.types.MoneyType;
import net.sf.xapp.net.common.types.UserId;
import ngpoker.moneysystem.types.Account;
import net.sf.xapp.net.server.playerrepository.SimpleUserStore;
import net.sf.xapp.net.server.playerrepository.User;
import net.sf.xapp.net.server.playerrepository.UserspaceCoordDeck;
import net.sf.xapp.net.server.framework.persistendb.DiffDetectorConverter;
import net.sf.xapp.net.server.framework.persistendb.FileDB;
import net.sf.xapp.net.server.framework.persistendb.FileDBImpl;
import ngpoker.common.util.deck.RandomEntropySource;
import ngpoker.user.*;
import org.jasypt.util.password.BasicPasswordEncryptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic operations direct on the player database
 */
public class PlayerTool
{
    public static void main(String[] args)
    {
        Map<UserId, User> userMap = loadUsers();
        for (User user : userMap.values())
        {
            Account account = user.getAccount(new AccountType(MoneyType.REAL_MONEY, "USD"));
            user.setAccountBalance(account, 0);
        }
    }

    public static void updateAccountBalance(int accountIndex, long amount)
    {
        Map<UserId, User> userMap = loadUsers();
        //update account balances
        for (User user : userMap.values())
        {
            user.setAccountBalance(accountIndex, amount);
        }
    }

    public static void createBots()
    {
        UserspaceCoordDeck deck = new UserspaceCoordDeck(new RandomEntropySource(), 100, 300, 100);
        BasicPasswordEncryptor encryptor = new BasicPasswordEncryptor();
        String password = encryptor.encryptPassword("test");
        FileDB<User, UserEntityListener> db = createUserDB();
        for(int i=0;i<50;i++)
        {
            String id = "bot_" + i;
            UserId userId = new UserId(id);
            Country country = Country.values()[i % Country.values().length];
            String email = id + "@botty.com";
            UserInfo userInfo = new UserInfo("botty_" + i,"botman", password, id, country, email);
            User user = SimpleUserStore.createUser(userId, userInfo, 0, 100000L, deck.draw(), false);
            db.add(id, user);
        }
    }

    private static Map<UserId, User> loadUsers()
    {
        FileDB<User, UserEntityListener> userDB = createUserDB();

        List<User> users = userDB.readAll();
        Map<UserId, User> userMap = new HashMap<UserId, User>();
        for (User user : users)
        {
            user.addListener(new UserEntityListenerAdaptor(user.getUserId().getValue(), userDB));
            userMap.put(user.getUserId(), user);
        }
        return userMap;
    }

    private static FileDB<User, UserEntityListener> createUserDB()
    {
        NodeInfo nodeInfo = new NodeInfoImpl(0, "_NG_BACKUP");
        FileDB<User, UserEntityListener> userDB = new FileDBImpl<User, UserEntityListener>(nodeInfo, User.class,
                "users", new DiffDetectorConverter(false), UserEntity.class);
        return userDB;
    }
}
