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
package net.sf.xapp.utils;

public class Tokenizer
{
    private String m_str;
    private String m_delimeter;

    public Tokenizer(String str)
    {
        this(str, ",");
    }
    public Tokenizer(String str, String delimeter)
    {
        m_str = str;
        m_delimeter = delimeter;
    }

    public String next()
    {
        if (m_str == null) return null;
        String[] args = m_str.split(m_delimeter, 2);
        String head = args[0];
        m_str = args.length == 2 ? args[1] : null;
        return head;
    }
}
