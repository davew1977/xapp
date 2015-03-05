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
package net.sf.xapp.application.core;

import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.ToolTipHandler;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.*;

import java.util.Collection;
import java.util.List;

public class DefaultToolTipHandler implements ToolTipHandler
{
    private ClassDatabase m_classDatabase;
    private boolean m_enabled = true;

    public DefaultToolTipHandler(ClassDatabase classDatabase)
    {
        m_classDatabase = classDatabase;
    }

    public void disable()
    {
        m_enabled = false;
    }

    public String getTooltip(Node objectNode)
    {
        ObjectMeta objectMeta = objectNode.objectMeta();
        if(objectMeta==null) {
            return null;
        }
        ClassModel classModel = objectMeta.getClassModel();
        Object instance = objectMeta.getInstance();
        return getTooltip(classModel, instance);
    }

    private String getTooltip(ClassModel classModel, Object instance) {
        if(!m_enabled)
        {
            return null;
        }
        StringBuffer str = new StringBuffer();
        str.append("<html><table><font size=2>");
        str.append("<tr><td><font size=2 COLOR=\"#000000\">Class</b></td><td><font size=2>").append(classModel).append("</td></tr>");
        List<Property> properties = classModel.getNonTransientProperties();
        List<ContainerProperty> listProperties = classModel.getNonTransientPrimitiveLists();
        for (Property property : properties)
        {
            String name = property.getName();
            String propvalue = String.valueOf(property.get(instance));
            if(propvalue!=null && propvalue.length()>50)
            {
                propvalue = propvalue.substring(0,50);
            }
            str.append("<tr><td><font size=2 COLOR=\"#000000\"><b>").append(name).append("</td><td><font size=2>").append(propvalue).append("</td></tr>");
        }
        for (ContainerProperty listProperty : listProperties)
        {
            String name = listProperty.getName();
            Collection list = listProperty.getCollection(instance);
            if (list!=null)
            {
                String info = "size="+list.size();
                str.append("<tr><td><font size=2 COLOR=\"#000000\"><b>").append(name).append("</td><td><font size=2>").append(info).append("</td></tr>");
            }
        }
        str.append("</table></html>");
        return str.toString();
    }

    public String getTooltip(Object obj)
    {
        return getTooltip(m_classDatabase.getClassModel(obj.getClass()), obj);
    }
}
