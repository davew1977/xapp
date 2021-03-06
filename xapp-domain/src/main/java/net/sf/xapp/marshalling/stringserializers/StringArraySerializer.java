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

public class StringArraySerializer implements StringSerializer<String[]>
{
    public String[] read(String str)
    {
        return str.split(",");
    }

    public String validate(String text)
    {
        return null;
    }

    public String write(String[] strs)
    {
        return _write(strs);
    }

    public static String _write(String[] strs)
    {
        StringBuilder sb = new StringBuilder();
        for (String s : strs)
        {
            sb.append(s).append(',');
        }
        return sb.toString();
    }
}
