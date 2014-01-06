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

public class PropertyChangeTuple
{
    public Property property;
    public Object target;
    public Object oldVal;
    public Object newVal;

    public PropertyChangeTuple(Property property, Object target, Object oldVal, Object newVal)
    {
        this.property = property;
        this.target = target;
        this.oldVal = oldVal;
        this.newVal = newVal;
    }


    @Override
    public String toString()
    {
        return "property "+property+" changed from "+oldVal +" to "+newVal + " in " + target;

    }
}
