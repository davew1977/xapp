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
package net.sf.xapp.objectmodelling.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * a potential property update
 */
public class PropertyUpdate
{
    public Property property;
    public Object oldVal;
    public Object newVal;

    public PropertyUpdate(Property property, Object oldVal, Object newVal)
    {
        this.property = property;
        this.oldVal = oldVal;
        this.newVal = newVal;
    }

    @Override
    public String toString()
    {
        return "property "+property+" to change from "+oldVal +" to "+newVal;
    }

    public static Map<String, PropertyChange> execute(ObjectMeta obj, List<PropertyUpdate> potentialUpdates) {
        Map<String, PropertyChange> result = new LinkedHashMap<String, PropertyChange>();
        for (PropertyUpdate potentialUpdate : potentialUpdates) {
            result.put(potentialUpdate.property.getName(), potentialUpdate.execute(obj));
        }
        return result;
    }

    private PropertyChange execute(ObjectMeta obj) {
        return obj.set(property, newVal);
    }

    public String getPropertyName() {
        return property.getName();
    }

    public String oldValAsString(ObjectMeta objectMeta) {
        return property.convert(objectMeta, oldVal);
    }

    public String newValAsString(ObjectMeta objectMeta) {
        return property.convert(objectMeta, newVal);
    }
}
