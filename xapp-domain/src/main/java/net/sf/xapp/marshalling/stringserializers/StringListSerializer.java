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
import net.sf.xapp.utils.StringUtils;

import java.util.List;

public class StringListSerializer implements StringSerializer<List<String>>
{
    public List<String> read(String str)
    {
        return StringUtils.convertToStringList(str);
    }

    public String write(List<String> ints)
    {
        return StringUtils.convertToString(ints);
    }

    public String validate(String text)
    {
        return null;
    }
}
