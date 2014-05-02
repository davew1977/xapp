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
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.objectmodelling.core.PropertyChangeTuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Used when the GUI is editing one object only
 */
public class SingleTargetEditableContext implements EditableContext
{
    public enum Mode
    {
        CREATE, EDIT
    }

    private ObjectMeta objMeta;
    private Mode m_mode;

    public SingleTargetEditableContext(ObjectMeta target, Mode mode)
    {
        objMeta = target;
        m_mode = mode;
    }

    public String getTitle()
    {
        return objMeta.getClassModel().toString();
    }

    public List<Property> getVisibleProperties()
    {
        return objMeta.getClassModel().getVisibleProperties();
    }

    public Object getPropertyValue(Property property)
    {
        return objMeta.getProp(property);
    }

    public List<PropertyChangeTuple> setPropertyValue(Property property, Object value)
    {
        List<PropertyChangeTuple> changes = new ArrayList<PropertyChangeTuple>();
        PropertyChangeTuple propertyChangeTuple = objMeta.setProp(property, value);
        if (propertyChangeTuple != null)
        {
            changes.add(propertyChangeTuple);
        }
        return changes;
    }

    public ObjectMeta getObjMeta()
    {
        return objMeta;
    }

    public ClassModel getClassModel()
    {
        return objMeta.getClassModel();
    }

    public boolean isCheckMandatoryFields()
    {
        return true;
    }

    public boolean isValidateFields()
    {
        return true;
    }

    public boolean isPropertyEditable(Property property)
    {
        return property.isEditableOnCreation() && m_mode == Mode.CREATE
                || property.isEditable() && m_mode == Mode.EDIT;
    }

    public Mode getMode()
    {
        return m_mode;
    }
}
