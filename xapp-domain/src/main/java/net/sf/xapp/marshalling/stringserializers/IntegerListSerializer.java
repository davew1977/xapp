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

import java.util.ArrayList;
import java.util.List;

public class IntegerListSerializer implements StringSerializer<List<Integer>>
{
    public List<Integer> read(String str)
    {
        return doRead(str);
    }

    public static List<Integer> doRead(String str)
    {
        List<Integer> ints = new ArrayList<Integer>();
        if(str==null || str.equals(""))
        {
            return ints;
        }
        String[] strs = str.split(",");
        for (String s : strs)
        {
            ints.add(Integer.parseInt(s));
        }
        return ints;
    }

    public String write(List<Integer> ints)
    {
        return doWrite(ints);
    }

    public static String doWrite(List<Integer> ints)
    {
        if(ints==null) ints = new ArrayList<Integer>();
        StringBuilder sb = new StringBuilder();
        for (Integer i: ints)
        {
            sb.append(i).append(',');
        }
        return sb.toString();
    }

    public String validate(String text)
    {
        return null;
    }
}
