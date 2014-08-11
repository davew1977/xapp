/*
 *
 * Date: 2010-sep-06
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.playerrepository;

import ngpoker.common.types.*;
import ngpoker.moneysystem.backend.BackendMoney;
import ngpoker.moneysystem.backend.to.GetAccountBalanceResponse;
import ngpoker.playerlookup.PlayerLookup;
import ngpoker.playerlookup.to.FindPlayerResponse;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SimplePlayerLookup implements PlayerLookup, BackendMoney
{
    private Logger log = Logger.getLogger(getClass());
    private Map<UserId, Player> playerMap;

    public SimplePlayerLookup()
    {
        playerMap = new HashMap<UserId, Player>();
        playerMap.put(new UserId("100"), new Player().deserialize("[[100],fergie,[10], Canada, false, false, a@b.com]"));
        playerMap.put(new UserId("101"), new Player().deserialize("[[101],gus_hanss,[10],Canada, false, false, a@b.com]"));
        playerMap.put(new UserId("102"), new Player().deserialize("[[102],negreanu,[10],Canada, false, false, a@b.com]"));
        playerMap.put(new UserId("103"), new Player().deserialize("[[103],phil_ivey,[10],Canada, false, false, a@b.com]"));
        playerMap.put(new UserId("104"), new Player().deserialize("[[104],sam_farha,[11],Canada, false, false, a@b.com]"));
        playerMap.put(new UserId("105"), new Player().deserialize("[[105],doyle,[11],Canada, false, false, a@b.com]"));
        playerMap.put(new UserId("106"), new Player().deserialize("[[106],amarillo,[11],Canada, false, false, false, a@b.com]"));
        playerMap.put(new UserId("107"), new Player().deserialize("[[107],moneymaker,[12],Canada, false, false, a@b.com]"));
        playerMap.put(new UserId("108"), new Player().deserialize("[[108],lederer,[12],Canada, false, false, a@b.com]"));
        playerMap.put(new UserId("109"), new Player().deserialize("[[109],jen_harman,[13],Canada, false, false, a@b.com]"));
        playerMap.put(new UserId("110"), new Player().deserialize("[[110],steve,[13],Canada, false, false, a@b.com]"));

        //add 100 bots
        for(int i=0; i<100; i++)
        {
            playerMap.put(new UserId(String.valueOf(10000 + i)), new Player().deserialize(
                    String.format("[[%s],bot_%s,[%s],Canada, false, false, a@b.com]", 10000+i, 10000+i, 10)));
        }
    }

    @Override
    public FindPlayerResponse findPlayer(UserId id) throws GenericException
    {
        log.debug("find player, " + id);
        Player player = playerMap.get(id);
        if(player==null)
        {
            throw new GenericException(ErrorCode.PLAYER_DOES_NOT_EXIST);
        }
        return new FindPlayerResponse(player);
    }

    public static void main(String[] args)
    {
        SimplePlayerLookup s = new SimplePlayerLookup();
        FindPlayerResponse r = s.findPlayer(new UserId("100"));
        System.out.println(r.serialize());
        s.findPlayer(new UserId("1"));
    }

    @Override
    public GetAccountBalanceResponse getAccountBalance(UserId userId, AccountType accountType) throws GenericException
    {
        return new GetAccountBalanceResponse(1000L);
    }

    @Override
    public void debitPlayer(UserId userId, AccountType accountType, Long amount) throws GenericException
    {
        if(amount>1000)
        {
            throw new GenericException(ErrorCode.NOT_ENOUGH_MONEY);
        }
    }

    @Override
    public void creditPlayer(UserId userId, AccountType accountType, Long amount) throws GenericException
    {

    }

    @Override
    public void creditOperator(OperatorId operatorId, AccountType accountType, Long amount) throws GenericException
    {

    }

    @Override
    public void setAccountBalance(UserId userId, AccountType accountType, Long amount) throws GenericException
    {

    }
}
