/*
 *
 * Date: 2010-sep-09
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * represents an api derived from one defined in the main model
 */
public class TransientApi extends Api
{
    private final TransientApiType type;
    private String packageName;

    public TransientApi(TransientApiType type)
    {
        this.type = type;
    }

    public TransientApi(Api original, TransientApiType apiType)
    {
        type = apiType;
        this.apiType = original.apiType;
        this.entityKeyType = original.entityKeyType;
        this.errors = original.errors;
        this.messages = original.messages;
        this.packageName = original.getPackageName();
        this.principalType = original.principalType;
        this.responseEntityKeyType = original.responseEntityKeyType;
        this.hideEntityKey = original.hideEntityKey;
        this.clientVisible = original.clientVisible;
        this.changedInSession = original.changedInSession;
        this.hidePrincipalField = original.hidePrincipalField;
        this.setName(original.getName());
        this.setModule(original.getModule());
    }
    public void init(Module module)
    {
        setModule(module);
        for (Message message : messages)
        {
            message.setApi(this);
            message.setPackageName(messagePackageName());
            message.setModule(module);
        }
    }

    private boolean isDefault()
    {
        return type == TransientApiType.DEFAULT;
    }

    @Override
    public boolean hasErrors()
    {
        return isDefault() && super.hasErrors();
    }

    public boolean isEmpty()
    {
        return messages.isEmpty();
    }

    public List<Message> deriveResponses()
    {
        List<Message> responses = new ArrayList<Message>();
        for (Message message : getMessages())
        {
            if (message.shouldGenerateSyncResponse())
            {
                Message response = message.getResponse().createMessage(null);
                response.setApi(this);
                response.setPackageName(messagePackageName());
                responses.add(response);
            }
        }
        return responses;
    }

    public List<Message> allMessagesAndResponses()
    {
        List<Message> messages = deriveResponses();
        messages.addAll(this.messages);
        return messages;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }
}
