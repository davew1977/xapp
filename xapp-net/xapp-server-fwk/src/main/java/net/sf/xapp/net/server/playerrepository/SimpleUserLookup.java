/*
 *
 * Date: 2010-sep-06
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import net.sf.xapp.net.api.userlookup.UserLookup;
import net.sf.xapp.net.api.userlookup.to.FindUserResponse;
import net.sf.xapp.net.common.types.ErrorCode;
import net.sf.xapp.net.common.types.GenericException;
import net.sf.xapp.net.common.types.User;
import net.sf.xapp.net.common.types.UserId;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SimpleUserLookup implements UserLookup
{
    private Logger log = Logger.getLogger(getClass());
    private Map<UserId, User> userMap;

    public SimpleUserLookup()
    {
        userMap = new HashMap<UserId, User>();
        userMap.put(new UserId("100"), new User().deserialize("[[100],fergie,[10], Canada, false, false, a@b.com]"));
        userMap.put(new UserId("101"), new User().deserialize("[[101],gus_hanss,[10],Canada, false, false, a@b.com]"));
        userMap.put(new UserId("102"), new User().deserialize("[[102],negreanu,[10],Canada, false, false, a@b.com]"));
        userMap.put(new UserId("103"), new User().deserialize("[[103],phil_ivey,[10],Canada, false, false, a@b.com]"));
        userMap.put(new UserId("104"), new User().deserialize("[[104],sam_farha,[11],Canada, false, false, a@b.com]"));
        userMap.put(new UserId("105"), new User().deserialize("[[105],doyle,[11],Canada, false, false, a@b.com]"));
        userMap.put(new UserId("106"), new User().deserialize("[[106],amarillo,[11],Canada, false, false, false, a@b.com]"));
        userMap.put(new UserId("107"), new User().deserialize("[[107],moneymaker,[12],Canada, false, false, a@b.com]"));
        userMap.put(new UserId("108"), new User().deserialize("[[108],lederer,[12],Canada, false, false, a@b.com]"));
        userMap.put(new UserId("109"), new User().deserialize("[[109],jen_harman,[13],Canada, false, false, a@b.com]"));
        userMap.put(new UserId("110"), new User().deserialize("[[110],steve,[13],Canada, false, false, a@b.com]"));

        //add 100 bots
        for(int i=0; i<100; i++)
        {
            userMap.put(new UserId(String.valueOf(10000 + i)), new User().deserialize(
                    String.format("[[%s],bot_%s,[%s],Canada, false, false, a@b.com]", 10000+i, 10000+i, 10)));
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

}
