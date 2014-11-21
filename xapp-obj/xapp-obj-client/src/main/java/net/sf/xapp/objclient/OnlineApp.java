package net.sf.xapp.objclient;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.SimpleApplication;
import net.sf.xapp.net.client.io.ConnectionListener;
import net.sf.xapp.net.common.types.ConnectionState;
import net.sf.xapp.objclient.ui.ConnectivityPane;
import net.sf.xapp.uifwk.Callback;

/**
 * base class for swing based online apps
 */
public class OnlineApp<T> extends SimpleApplication<T> implements ConnectionListener {
    private ConnectivityPane connectivityPane;
    private GUIObjClient objClient;

    public OnlineApp(GUIObjClient objClient) {
        this.objClient = objClient;
        objClient.addConnectionListener(this);
    }

    @Override
    public void init(ApplicationContainer<T> applicationContainer) {
        super.init(applicationContainer);

        connectivityPane = new ConnectivityPane(new Callback() {
            @Override
            public void call(Object... args) {
                ConnectionState requestedState = (ConnectionState) args[0];
                if(requestedState == ConnectionState.OFFLINE) {
                    objClient.setOffline();
                } else {
                    objClient.connect();
                }
            }
        }, objClient.getClientContext());
        applicationContainer.getToolBar().add(connectivityPane);
        connectivityPane.setConnectionState(objClient.isConnected() ? ConnectionState.ONLINE : ConnectionState.OFFLINE);

    }

    @Override
    public void connectionStateChanged(ConnectionState newState) {
        connectivityPane.setConnectionState(newState);
    }

    @Override
    public void handleConnectException(Exception e) {

    }
}
