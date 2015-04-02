/*
 *
 * Date: 2010-jun-02
 * Author: davidw
 *
 */
package xapp.mdda.model;

import net.sf.xapp.annotations.application.Container;

import java.util.ArrayList;
import java.util.List;

@Container(listProperty = "messages")
public class Api extends AbstractArtifact implements Artifact {
    protected List<Message> messages = new ArrayList<Message>();
    protected ApiType apiType = ApiType.ASYNC;

    public Api() {
    }

    public ApiType getApiType() {
        return apiType;
    }

    public void setApiType(ApiType apiType) {
        this.apiType = apiType;
    }


    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }



    @Override
    public Api clone() {
        try {
            Api api = (Api) super.clone();
            api.messages = new ArrayList<>();
            for (Message message : messages) {
                api.messages.add(message.clone());
            }
            return api;
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
