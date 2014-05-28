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
    public ObjectMeta target;
    public Object oldVal;
    public Object newVal;

    public PropertyUpdate(Property property, ObjectMeta target, Object oldVal, Object newVal)
    {
        this.property = property;
        this.target = target;
        this.oldVal = oldVal;
        this.newVal = newVal;
    }

    @Override
    public String toString()
    {
        return "property "+property+" to change from "+oldVal +" to "+newVal + " in " + target;

    }

    public static Map<String, PropertyChange> execute(List<PropertyUpdate> potentialUpdates) {
        Map<String, PropertyChange> result = new LinkedHashMap<String, PropertyChange>();
        for (PropertyUpdate potentialUpdate : potentialUpdates) {
            result.put(potentialUpdate.property.getName(), potentialUpdate.execute());
        }
        return result;
    }

    private PropertyChange execute() {
        return target.set(property, newVal);
    }
}
