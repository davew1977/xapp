/*
 * Xapp (pronounced Zap!), A automatic gui tool for Java.
 * Copyright (C) 2009 David Webber. All Rights Reserved.
 *
 * The contents of this file may be used under the terms of the GNU Lesser
 * General Public License Version 2.1 or later.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 */
package net.sf.xapp.marshalling.stringserializers;

import net.sf.xapp.marshalling.api.StringSerializer;
import net.sf.xapp.utils.XappException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnumListSerializer implements StringSerializer<List<? extends Enum>>
{
    private List<Enum> m_enumValues;

    public EnumListSerializer(Class enumClass)
    {
        Enum[] values = getEnumValues(enumClass);
        m_enumValues = Arrays.asList(values);
    }

    public List<? extends Enum> read(String str)
    {
        return doRead(str, m_enumValues);
    }

    public static List<? extends Enum> doRead(String str, Class enumClass)
    {
        Enum[] values = getEnumValues(enumClass);
        return doRead(str, Arrays.asList(values));
    }
    private static List<? extends Enum> doRead(String str, List<Enum> m_enumValues)
    {
        ArrayList<Enum> e = new ArrayList<Enum>();
        if(str==null)
        {
            return e;
        }
        String[] items = str.split(",");
        for (String item : items)
        {
            for (Enum enumValue : m_enumValues)
            {
                if(enumValue.toString().equals(item))
                {
                    e.add(enumValue);
                }
            }
        }
        return e;
    }

    public String write(List<? extends Enum> obj)
    {
        return doWrite(obj);
    }

    public static String doWrite(List<? extends Enum> obj)
    {
        if(obj==null || obj.isEmpty())
        {
            return "";
        }
        StringBuilder sb = new  StringBuilder();
        for (Enum anEnum : obj)
        {
            sb.append(anEnum.toString()).append(",");
        }
        return sb.toString();
    }

    public String validate(String text)
    {
        return null;
    }


    public static Enum[] getEnumValues(Class propertyClass)
    {
        try
        {
            Method method = propertyClass.getMethod("values");
            method.setAccessible(true);
            return (Enum[]) method.invoke(null);
        }
        catch (Exception e)
        {
            throw new XappException(e);
        }
    }
}
