/*
 *
 * Date: 2010-jun-02
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.application.Container;
import net.sf.xapp.annotations.objectmodelling.Key;
import net.sf.xapp.annotations.objectmodelling.Reference;
import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.tree.TreeNode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Container(listProperty = "messages")
public class Api extends AbstractArtifact implements Artifact
{
    protected String name;
    protected List<Message> messages = new ArrayList<Message>();
    protected List<String> errors = new ArrayList<String>();
    protected Type entityKeyType;
    protected Type principalType;
    protected Type responseEntityKeyType;
    protected ApiType apiType = ApiType.ASYNC;
    protected boolean hideEntityKey;
    protected boolean hidePrincipalField;
    protected boolean clientVisible;
    protected Module module;

    public Api()
    {
    }

    public ApiType getApiType()
    {
        return apiType;
    }

    public void setApiType(ApiType apiType)
    {
        this.apiType = apiType;
    }

    public boolean hasPrincipal()
    {
        return getPrincipalType()!=null;
    }

    @Reference
    public Type getPrincipalType()
    {
        return principalType;
    }

    public void setPrincipalType(Type principalType)
    {
        this.principalType = principalType;
    }

    @Transient
    public boolean isEntity()
    {
        return entityKeyType!=null;
    }

    @Reference
    public Type getEntityKeyType()
    {
        return entityKeyType;
    }

    public void setEntityKeyType(Type entityKeyType)
    {
        this.entityKeyType = entityKeyType;
    }
    
    @Reference
    public Type getResponseEntityKeyType()
    {
        return responseEntityKeyType;
    }

    public void setResponseEntityKeyType(Type responseEntityKeyType)
    {
        this.responseEntityKeyType = responseEntityKeyType;
    }

    public List<Message> getMessages()
    {
        return messages;
    }

    public void setMessages(List<Message> messages)
    {
        this.messages = messages;
    }

    public void validate(List<String> errorList)
    {
        for (Message message : messages)
        {
            errorList.addAll(message.validate());
        }
    }

    /**
     * api is synchronous if one or more "methods" has a return value
     *
     * @return
     */
    @Transient
    public boolean isSynchronous()
    {
        return apiType.isSynchronous();
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public void setErrors(List<String> errors)
    {
        this.errors = errors;
    }

    public Field entityKeyField()
    {
        Field f = new EntityKeyField();
        f.setType(getEntityKeyType());
        f.setName("key");
        f.setOptional(true);
        return f;
    }

    public boolean hasErrors()
    {
        return getErrors()!=null && !getErrors().isEmpty();
    }

    public Field principalField()
    {
        Field f = new PrincipalField();
        f.setType(getPrincipalType());
        return f;
    }

    @Override
    public Api clone()
    {
        try
        {
            Api api = (Api) super.clone();
            api.messages = new ArrayList<Message>();
            for (Message message : messages)
            {
                api.messages.add(message.clone());
            }
            return api;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void add(Message m)
    {
        getMessages().add(m);
    }

    public Set<String> resolvePackages()
    {
        Set<String> h = new LinkedHashSet<String>();
        for (Message message : messages)
        {
            h.addAll(message.resolvePackages());
        }
        if(entityKeyType!=null)
        {
            h.add(entityKeyType.getPackageName());
        }
        if(principalType!=null)
        {
            h.add(principalType.getPackageName());
        }
        return h;
    }

    public boolean isHideEntityKey()
    {
        return hideEntityKey;
    }

    public void setHideEntityKey(boolean hideEntityKey)
    {
        this.hideEntityKey = hideEntityKey;
    }

    public String messagePackageName()
    {
        return getPackageName() + ".to";
    }

    public boolean isClientVisible()
    {
        return clientVisible;
    }

    public void setClientVisible(boolean clientVisible)
    {
        this.clientVisible = clientVisible;
    }

    public List<TransientApi> deriveApis() {

        ArrayList<TransientApi> results = new ArrayList<TransientApi>();
        results.add(new TransientApi(this, TransientApiType.DEFAULT));
        if (getApiType().hasReplyApi())
        {
            TransientApi replyApi = new TransientApi(TransientApiType.REPLY);
            replyApi.setName(getName() + "Reply");
            replyApi.setPackageName(getPackageName());
            replyApi.setEntityKeyType(getResponseEntityKeyType());
            replyApi.setPrincipalType(getPrincipalType());
            replyApi.setApiType(getApiType());
            replyApi.setHideEntityKey(isHideEntityKey());
            replyApi.setClientVisible(isClientVisible());
            replyApi.setChangedInSession(isChangedInSession());
            for (Message message : getMessages())
            {
                Message response = message.getResponse().createMessage(null);
                response.addField(new Field("errorCode", "ErrorCode", EnumType.class));
                for (Field field : response.getFields())
                {
                    field.setOptional(true);
                }
                replyApi.add(response);
            }
            replyApi.init(getModule());
            results.add(replyApi);
        }
        return results;
    }

    public boolean isHidePrincipalField() {
        return hidePrincipalField;
    }

    public void setHidePrincipalField(boolean hidePrincipalField) {
        this.hidePrincipalField = hidePrincipalField;
    }
}
