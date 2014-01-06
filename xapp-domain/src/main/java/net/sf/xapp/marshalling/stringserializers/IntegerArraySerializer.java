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

public class IntegerArraySerializer implements StringSerializer<int[]>
{
    public int[] read(String str)
    {
        return doRead(str);
    }

    public static int[] doRead(String str)
    {
        if(str==null)return null;
        String[] strs = str.split(",");
        int[] ints = new int[strs.length];
        for (int i = 0; i < strs.length; i++)
        {
            String s = strs[i];
            ints[i] = Integer.parseInt(s);
        }
        return ints;
    }

    public String write(int[] ints)
    {
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
