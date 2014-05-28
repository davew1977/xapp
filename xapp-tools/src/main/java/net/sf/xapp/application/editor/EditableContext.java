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

import net.sf.xapp.objectmodelling.core.*;
import net.sf.xapp.objectmodelling.core.PropertyChange;

import java.util.List;

public interface EditableContext
{
    String getTitle();

    List<Property> getVisibleProperties();

    Object getPropertyValue(Property property);

    /**
     *
     * @param property
     * @param value
     * @return changes, or empty of no changes
     */
    List<PropertyUpdate> potentialUpdates(Property property, Object value);

    /**
     * The target is the object owning the modified properties.
     * @return
     */
    ObjectMeta getObjMeta();

    ClassModel getClassModel();

    boolean isCheckMandatoryFields();

    boolean isValidateFields();

    boolean isPropertyEditable(Property property);

}
