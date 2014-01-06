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
package net.sf.xapp.marshalling.namevaluepair;

public class SimpleNameValuePair extends ComparableNameValuePair
{
    private String m_name;
    private String m_value;

    public SimpleNameValuePair(String name, String value)
    {
        m_name = name;
        m_value = value;
    }

    public String getName()
    {
        return m_name;
    }

    public String getValue()
    {
        return m_value;
    }
}
