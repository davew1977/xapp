/*
 *
 * Date: 2010-sep-10
 * Author: davidw
 *
 */
package net.sf.xapp.net.common.framework;

import ngpoker.common.types.MessageTypeEnum;
import net.sf.xapp.net.common.util.StringUtils;

import java.io.*;
import java.util.List;

public class TransportHelper
{
    public static byte[] toByteArray(Message message)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutput dos = new DataOutputStream(baos);
            dos.writeInt(message.type().getId());
            message.writeData(dos);
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    public static byte[] toByteArray(TransportObject obj)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutput dos = new DataOutputStream(baos);
            obj.writeData(dos);
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    public static <T extends TransportObject> T  fromByteArray(Class<T> aClass, byte[] bytes)
    {
        try
        {
            T obj = aClass.newInstance();
            readObj(obj, bytes);
            return obj;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    public static <T extends TransportObject> T  readObj(T obj, byte[] bytes)
    {
        try
        {
            DataInput din = new DataInputStream(new ByteArrayInputStream(bytes));
            obj.readData(din);
            return obj;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Message> T copy(T message)
    {
        byte[] bytes = toByteArray(message);
        return (T)fromByteArray(bytes);
    }

    public static <T extends TransportObject> T fromByteArray(byte[] bytes)
    {
        try
        {
            DataInput din = new DataInputStream(new ByteArrayInputStream(bytes));
            int type = din.readInt();
            TransportObject message = ng.Global.create(type);
            message.readData(din);
            return (T) message;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String toString(Message m)
    {
        return m.type().name() + "," + m.serialize();
    }

    public static <T extends Message> T fromString(String s)
    {
        List<Object> data = StringUtils.parse(s);
        T message = (T) ng.Global.create((String) data.get(0));
        message.populateFrom((List)data.get(1));
        return message;
    }

    public static <E extends TransportObject> E copy(Class<E> aClass, E obj)
    {
        return fromByteArray(aClass, toByteArray(obj));
    }
}
