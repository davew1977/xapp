package net.sf.xapp.objclient.ui;


import javax.swing.*;
import java.awt.*;

import net.sf.xapp.objclient.ObjClientContext;
import net.sf.xapp.uifwk.Callback;
import net.sf.xapp.net.common.types.ConnectionState;
import net.sf.xapp.uifwk.XPane;

import static net.sf.xapp.net.common.types.ConnectionState.*;

/**
 * icon representing application's connection state
 */
public class ConnectivityPane extends XPane {
    private static Image IMAGE_ONLINE = new ImageIcon(ConnectivityPane.class.getResource("/images/online.png")).getImage();
    private static Image IMAGE_OFFLINE = new ImageIcon(ConnectivityPane.class.getResource("/images/offline.png")).getImage();
    private static Image IMAGE_CONNECTING = new ImageIcon(ConnectivityPane.class.getResource("/images/connecting.png")).getImage();
    private static Image IMAGE_CONNECTION_LOST = new ImageIcon(ConnectivityPane.class.getResource("/images/disconnected.png")).getImage();
    private final ObjClientContext clientContext;

    public ConnectivityPane(final Callback listener, final ObjClientContext clientContext) {
        setSize(16,16);
        this.clientContext = clientContext;
        addOnMouseClick(new Callback() {
            @Override
            public void call(Object... args) {
                switch (clientContext.getConnectionState()) {
                    case OFFLINE:
                        listener.call(ONLINE);
                        break;
                    case ONLINE:
                    case CONNECTING:
                    case CONNECTION_LOST:
                        listener.call(OFFLINE);
                        break;
                }
            }
        });
    }

    public void setConnectionState(ConnectionState connectionState) {
        setToolTipText(clientContext.isOnlineMode()? "Click to go Offline" : "Click to go Online");
        repaint();
    }

    @Override
    protected void paintPane(Graphics2D g) {
        Image im = getImage();
        g.drawImage(im, 0,0, null);
    }

    private Image getImage() {
        switch (clientContext.getConnectionState()) {
            case ONLINE: return IMAGE_ONLINE;
            case OFFLINE: return IMAGE_OFFLINE;
            case CONNECTING: return IMAGE_CONNECTING;
            case CONNECTION_LOST: return IMAGE_CONNECTION_LOST;
        }
        throw new IllegalArgumentException();
    }

}
