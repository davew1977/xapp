/*
 *
 * Date: 2010-sep-09
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.clustering;

import ngpoker.backend.useradmin.UserAdmin;
import ngpoker.backend.userapi.UserApi;
import ngpoker.cashgameadmin.CashGameAdmin;
import ngpoker.forum.admin.ForumAdmin;
import ngpoker.handhistory.HandHistoryClient;
import ngpoker.infrastructure.types.NodeId;
import ngpoker.moneysystem.backend.BackendMoney;
import ngpoker.playerlookup.PlayerLookup;
import ngpoker.sng.sngadmin.SngAdmin;
import ngpoker.tournament.touradmin.TourAdmin;

import java.util.HashMap;
import java.util.Map;

public class StaticServiceLookup implements ServiceLookup {
    private final Map<Class, NodeId> serviceMap;

    public StaticServiceLookup(Map<Class, String> serviceMapStr) {
        this.serviceMap = new HashMap<Class, NodeId>();
        for (Map.Entry<Class, String> e : serviceMapStr.entrySet()) {
            serviceMap.put(e.getKey(), new NodeId(e.getValue()));
        }
    }

    public StaticServiceLookup() {
        serviceMap = new HashMap<Class, NodeId>();
        serviceMap.put(PlayerLookup.class, new NodeId("1"));
        serviceMap.put(CashGameAdmin.class, new NodeId("0"));
        serviceMap.put(TourAdmin.class, new NodeId("0"));
        serviceMap.put(SngAdmin.class, new NodeId("0"));
        serviceMap.put(UserApi.class, new NodeId("0"));
        serviceMap.put(UserAdmin.class, new NodeId("0"));
        serviceMap.put(BackendMoney.class, new NodeId("0"));
        serviceMap.put(HandHistoryClient.class, new NodeId("0"));
        serviceMap.put(ForumAdmin.class, new NodeId("0"));
    }

    @Override
    public NodeId lookupService(Class api) {
        NodeId nodeId = serviceMap.get(api);
        if (nodeId == null) {
            throw new RuntimeException("cannot find service " + api + " in cluster");
        }
        return nodeId;
    }
}
