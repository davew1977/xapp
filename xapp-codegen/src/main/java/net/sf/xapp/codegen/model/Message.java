/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.codegen.model;

import net.sf.xapp.annotations.objectmodelling.Transient;

import java.util.List;

public class Message extends ValueObject
{
    private boolean persistent;
    private Response response = new Response();
    public Api api;
    private Type returnType;

    @Override
    public void setName(String name)
    {
        super.setName(name);
        response.setName(name + "Response");
    }

    public Response getResponse()
    {
        return response;
    }

    public void setResponse(Response response)
    {
        this.response = response;
    }

    @Override
    public List<Field> resolveFields(boolean includeSuperFields)
    {
        List<Field> fields = resolveFieldsWithPrincipal(includeSuperFields);
        if(api.isEntity())
        {
            fields.add(0, api.entityKeyField());
        }
        return fields;
    }

    private List<Field> resolveFieldsWithPrincipal(boolean includeSuperFields)
    {
        List<Field> fields = super.resolveFields(includeSuperFields);
        if(api.hasPrincipal())
        {
            fields.add(0, api.principalField());
        }
        return fields;
    }

    public void setApi(Api api)
    {
        this.api = api;
        response.setModule(getModule());
    }

    @Override
    public Message clone() throws CloneNotSupportedException
    {
        return (Message) super.clone();
    }

    public static Message create(String name, List<Field> fields)
    {
        Message m = new Message();
        m.setName(name);
        m.getFields().addAll(fields);
        return m;
    }

    public boolean hasReturnValue()
    {
        return getReturnType()!=null || shouldGenerateSyncResponse();
    }

    public String uniqueObjectKey()
    {
        return api.getName() + "_" + getName();
    }

    public boolean shouldGenerateSyncResponse()
    {
        return api.isSynchronous() && !response.getFields().isEmpty();
    }

    /**
     * true if the message MUST be delivered, even if later
     * @return
     */
    public boolean isPersistent()
    {
        return persistent;
    }

    public Type getReturnType()
    {
        return returnType;
    }

    public void setReturnType(Type type)
    {
        returnType =  type;
    }

    public String returnType()
    {
        return hasReturnValue() ? 
                getReturnType()!=null ? getReturnType().getName() :
                getName() + "Response" : "void";
    }

    public String genericReturnType()
    {
        String rt = returnType();
        return rt.equals("void") ? "Void" : rt;
    }
}