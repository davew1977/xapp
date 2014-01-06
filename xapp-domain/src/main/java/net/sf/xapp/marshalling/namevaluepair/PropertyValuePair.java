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

import net.sf.xapp.objectmodelling.core.Property;

public class PropertyValuePair extends ComparableNameValuePair
{
    private Property m_property;
    private Object m_value;

    public PropertyValuePair(Property property, Object value)
    {
        m_property = property;
        m_value = value;
    }

    public String getName()
    {
        return m_property.getXMLMapping();
    }

    public Object getValue()
    {
        return m_value;
    }

    public Property getProperty()
    {
        return m_property;
    }
}
