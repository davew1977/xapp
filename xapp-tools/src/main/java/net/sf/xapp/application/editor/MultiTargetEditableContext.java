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
package net.sf.xapp.application.editor;

import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.objectmodelling.core.PropertyChangeTuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used when the GUI is editing one object only
 */
public class MultiTargetEditableContext implements EditableContext
{
    private ClassModel m_classModel;
    private List<Object> m_targets;
    private Map<Property, Object> m_latestValueMap = new HashMap<Property, Object>();

    public MultiTargetEditableContext(ClassModel classModel, List<Object> targets)
    {
        m_classModel = classModel;
        m_targets = targets;
    }

    public String getTitle()
    {
        return m_targets.size() + " instances of " + m_classModel.toString();
    }

    public List<Property> getVisibleProperties()
    {
        return m_classModel.getVisibleProperties();
    }

    public Object getPropertyValue(Property property)
    {
        //if the property value for all instances is the same then return that value
        Object controlValue = property.get(getTarget());
        for (int i = 1; i < m_targets.size(); i++)
        {
            Object o = property.get(m_targets.get(i));
            if (!Property.objEquals(controlValue, o))
            {
                controlValue = null;
                break;
            }
        }
        //put value in map
        m_latestValueMap.put(property, controlValue);
        return controlValue;
    }

    public List<PropertyChangeTuple> setPropertyValue(Property property, Object value)
    {
        ArrayList<PropertyChangeTuple> changes = new ArrayList<PropertyChangeTuple>();
        //if the value has changed then set it to all instances in list
        if (!Property.objEquals(value, m_latestValueMap.get(property)))
        {
            for (Object target : m_targets)
            {
                PropertyChangeTuple changeTuple = property.set(target, value);
                if (changeTuple!=null)
                {
                    changes.add(changeTuple);
                }
            }
        }
        //else do nothing
        return changes;
    }

    public Object getTarget()
    {
        return m_targets.get(0);
    }

    public ClassModel getClassModel()
    {
        return m_classModel;
    }

    public boolean isCheckMandatoryFields()
    {
        return false;
    }

    public boolean isValidateFields()
    {
        return false;
    }

    public boolean isCloseOnSave()
    {
        return true;
    }

    public boolean isPropertyEditable(Property property)
    {
        return property.isEditable();
    }
}
