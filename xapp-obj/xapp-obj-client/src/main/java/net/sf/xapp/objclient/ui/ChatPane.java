/*
 *
 * Date: 2010-dec-20
 * Author: davidw
 *
 */
package net.sf.xapp.objclient.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.*;
import java.util.List;

import net.sf.xapp.net.api.chatclient.ChatClient;
import net.sf.xapp.net.api.chatclient.to.ChatBroadcast;
import net.sf.xapp.net.api.chatuser.ChatUser;
import net.sf.xapp.net.client.framework.Callback;
import net.sf.xapp.net.client.framework.ClientContext;
import net.sf.xapp.net.common.types.UserId;
import net.sf.xapp.uifwk.GeneralListModel;
import net.sf.xapp.uifwk.XList;
import net.sf.xapp.uifwk.XPane;
import net.sf.xapp.uifwk.XScrollPane;
import net.sf.xapp.uifwk.XTextField;
import net.sf.xapp.uifwk.XTextFieldUI;

public class ChatPane extends XPane implements ChatClient, ChatUser {
    private XList chatItems;
    private GeneralListModel<ChatBroadcast> messages;
    private XTextField tf;
    private XScrollPane messageSP;
    private ClientContext clientContext;
    private java.util.List<Callback> listeners = new ArrayList<Callback>();

    public ChatPane(ClientContext clientContext) {
        this.clientContext = clientContext;
        chatItems = new XList();
        messages = new GeneralListModel<ChatBroadcast>();
        chatItems.setModel(messages);

        messageSP = new XScrollPane(chatItems);

        add(messageSP);
        tf = new XTextField("chat here!");
        tf.setFont(Font.decode("Tahoma-10"));
        tf.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
        tf.setUI(new XTextFieldUI(tf) {
            @Override
            public void paintBgrd(Graphics g, JTextField tf) {
                g.setColor(new Color(0, 0, 0, 128));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(255, 255, 255, 128));
                g.drawRect(0, 0, getWidth() - 1, tf.getHeight() - 1);
            }
        });
        tf.setForeground(Color.white);
        tf.setCaretColor(Color.white);
        add(tf);
        tf.addListener(this, "messageTyped");

        revalidate();
    }

    public void addListener(Callback listener) {
        this.listeners.add(listener);
    }

    @Override
    protected void paintPane(Graphics2D g) {
        setAlpha(g, getDefaultAlpha() * 0.4f);
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight() - tf.getHeight());
        setAlpha(g, getDefaultAlpha() * 1);
    }

    @Override
    public void setSize(int w, int h) {
        super.setSize(w, h);
        setSizeInternal(w, h);
    }

    private void setSizeInternal(int w, int h) {
        tf.setSize(w, 20);
        tf.setLocation(0, h - 20);
        messageSP.setSize(w, h - 20);
        chatItems.setCellRenderer(new ChatMessageCellRenderer(w, messageSP, clientContext));
    }


    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        setSizeInternal(width, height);
    }

    public void messageTyped() {
        String message = tf.getText();
        tf.setText("");
        for (Callback listener : listeners) {
            listener.call(message);
        }
    }

    public void init(java.util.List<ChatBroadcast> messages) {
        this.messages.clear();
        for (ChatBroadcast message : messages) {
            addMessage(message);
        }
    }

    public void addMessage(ChatBroadcast message) {
        messages.add(message);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final Rectangle b = chatItems.getBounds();
                chatItems.scrollRectToVisible(new Rectangle(0, b.height - 10, b.width, 10));
            }
        });

    }

    public void removeMessage(ChatBroadcast removed) {
        messages.remove(removed);
    }

    @Override
    public void chatBroadcast(UserId userId, String message, String senderNickname) {
        addMessage(new ChatBroadcast(null, userId, message, senderNickname));
    }

    @Override
    public void userJoined(UserId userId, String nickname) {
        addMessage(new ChatBroadcast(null, userId, "JOINED", nickname));

    }



    @Override
    public void userLeft(UserId userId, String nickname) {
        addMessage(new ChatBroadcast(null, userId, "LEFT", nickname));
    }

    @Override
    public void chatChannelState(UserId principal, Map<UserId, String> members) {
        for (Map.Entry<UserId, String> entry : members.entrySet()) {
            addMessage(new ChatBroadcast(null, entry.getKey(), "logged in", entry.getValue()));
        }
    }
}
