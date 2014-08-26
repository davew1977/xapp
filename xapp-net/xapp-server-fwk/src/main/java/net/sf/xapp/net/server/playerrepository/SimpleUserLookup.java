/*
 *
 * Date: 2010-sep-06
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import net.sf.xapp.net.api.userlookup.UserLookup;
import net.sf.xapp.net.api.userlookup.to.FindUserResponse;
import net.sf.xapp.net.common.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SimpleUserLookup implements UserLookup, UserStore
{
    private Logger log = LoggerFactory.getLogger(getClass());
    private Map<UserId, User> userMap;

    public SimpleUserLookup()
    {
        userMap = new HashMap<UserId, User>();
        userMap.put(new UserId("100"), new User().deserialize("[[100],fergie, false, false, a@b.com, Canada]"));
        userMap.put(new UserId("101"), new User().deserialize("[[101],gus_hanss, false, false, a@b.com, Canada]"));
        userMap.put(new UserId("102"), new User().deserialize("[[102],negreanu, false, false, a@b.com, Canada]"));
        userMap.put(new UserId("103"), new User().deserialize("[[103],phil_ivey, false, false, a@b.com, Canada]"));
        userMap.put(new UserId("104"), new User().deserialize("[[104],sam_farha, false, false, a@b.com, Canada]"));
        userMap.put(new UserId("105"), new User().deserialize("[[105],doyle, false, false, a@b.com, Canada]"));
        userMap.put(new UserId("106"), new User().deserialize("[[106],amarillo, false, false, a@b.com, Canada]"));
        userMap.put(new UserId("107"), new User().deserialize("[[107],moneymaker, false, false, a@b.com, Canada]"));
        userMap.put(new UserId("108"), new User().deserialize("[[108],lederer, false, false, a@b.com, Canada]"));
        userMap.put(new UserId("109"), new User().deserialize("[[109],jen_harman, false, false, a@b.com, Canada]"));
        userMap.put(new UserId("110"), new User().deserialize("[[110],steve, false, false, a@b.com, Canada]"));

        //add 100 bots
        for(int i=0; i<100; i++)
        {
            userMap.put(new UserId(String.valueOf(10000 + i)), new User().deserialize(
                    String.format("[[%s],bot_%s, false, false, a@b.com, Canada]", 10000+i, 10000+i)));
        }
    }

    @Override
    public FindUserResponse findUser(UserId id) throws GenericException
    {
        log.debug("find user, " + id);
        User user = userMap.get(id);
        if(user==null)
        {
            throw new GenericException(ErrorCode.USER_NOT_FOUND);
        }
        return new FindUserResponse(user);
    }

    public static void main(String[] args)
    {
        SimpleUserLookup s = new SimpleUserLookup();
        FindUserResponse r = s.findUser(new UserId("100"));
        System.out.println(r.serialize());
        s.findUser(new UserId("1"));
    }

    @Override
    public UserId addUser(UserInfo userInfo, ImageData profileImage) {
        return null;
    }

    @Override
    public UserId authenticateUser(String nickname, String password) {
        return null;
    }

    @Override
    public UserId authenticateUser(UserId userId, String keyToken, boolean bot) {
        return null;
    }

    @Override
    public UserId obtainGuestId(String nickname) {
        return null;
    }

    @Override
    public void resetPassword(String nickname, String email) {

    }

    @Override
    public UserEntityWrapper getUser(UserId userId) {
        return null;
    }

    @Override
    public UserId getUserId(String nickname) {
        return null;
    }
}
