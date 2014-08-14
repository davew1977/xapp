/*
 *
 * Date: 2010-sep-13
 * Author: davidw
 *
 */
package net.sf.xapp.net.server.chat;

import net.sf.xapp.net.api.chatappadmin.ChatAppAdmin;
import net.sf.xapp.net.common.types.Language;

import javax.annotation.PostConstruct;

public class ChatChannelFactory {
    private final ChatAppAdmin chatAppAdmin;
    private final String channelKey;

    public ChatChannelFactory(ChatAppAdmin chatAppAdmin, String channelKey) {
        this.chatAppAdmin = chatAppAdmin;
        this.channelKey = channelKey;
    }

    @PostConstruct
    public void init() {
        chatAppAdmin.create(channelKey, Language.ENGLISH, -1, channelKey);
    }

}
