package net.sf.xapp.net.server.minitools;

import net.sf.xapp.net.api.userentity.UserEntity;
import net.sf.xapp.net.api.userentity.UserEntityListener;
import net.sf.xapp.net.api.userentity.UserEntityListenerAdaptor;
import net.sf.xapp.net.common.types.Country;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.net.common.types.UserInfo;
import net.sf.xapp.net.common.util.random.RandomEntropySource;
import net.sf.xapp.net.server.clustering.NodeInfo;
import net.sf.xapp.net.server.clustering.NodeInfoImpl;
import net.sf.xapp.net.server.framework.persistendb.DiffDetectorConverter;
import net.sf.xapp.net.server.framework.persistendb.FileDB;
import net.sf.xapp.net.server.framework.persistendb.FileDBImpl;
import net.sf.xapp.net.server.playerrepository.SimpleUserStore;
import net.sf.xapp.net.server.playerrepository.UserEntityWrapper;
import net.sf.xapp.net.server.playerrepository.UserspaceCoordDeck;
import org.jasypt.util.password.BasicPasswordEncryptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic operations direct on the player database
 */
public class UserTool
{
    public static void main(String[] args)
    {
    }

    public static void createBots()
    {
        UserspaceCoordDeck deck = new UserspaceCoordDeck(new RandomEntropySource(), 100, 300, 100);
        BasicPasswordEncryptor encryptor = new BasicPasswordEncryptor();
        String password = encryptor.encryptPassword("test");
        FileDB<UserEntityWrapper, UserEntityListener> db = createUserDB();
        for(int i=0;i<50;i++)
        {
            String id = "bot_" + i;
            UserId userId = new UserId(id);
            Country country = Country.values()[i % Country.values().length];
            String email = id + "@botty.com";
            UserInfo userInfo = new UserInfo("botty_" + i,"botman", password, id, country, email);
            UserEntityWrapper user = SimpleUserStore.createUser(userId, userInfo, deck.draw(), false);
            db.add(id, user);
        }
    }

    private static Map<UserId, UserEntityWrapper> loadUsers()
    {
        FileDB<UserEntityWrapper, UserEntityListener> userDB = createUserDB();

        List<UserEntityWrapper> users = userDB.readAll();
        Map<UserId, UserEntityWrapper> userMap = new HashMap<UserId, UserEntityWrapper>();
        for (UserEntityWrapper user : users)
        {
            user.addListener(new UserEntityListenerAdaptor(user.getUserId().getValue(), userDB));
            userMap.put(user.getUserId(), user);
        }
        return userMap;
    }

    private static FileDB<UserEntityWrapper, UserEntityListener> createUserDB()
    {
        NodeInfo nodeInfo = new NodeInfoImpl(0, "_NG_BACKUP");
        FileDB<UserEntityWrapper, UserEntityListener> userDB = new FileDBImpl<UserEntityWrapper, UserEntityListener>(nodeInfo, UserEntityWrapper.class,
                "users", new DiffDetectorConverter(false), UserEntity.class);
        return userDB;
    }
}
