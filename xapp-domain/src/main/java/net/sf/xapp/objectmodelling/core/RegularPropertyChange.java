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

public class RegularPropertyChange extends AbstractPropertyChange {
    public final Object oldVal;
    public final Object newVal;

    public RegularPropertyChange(Property property, Object target, Object oldVal, Object newVal)
    {
        super(property, target);
        this.oldVal = oldVal;
        this.newVal = newVal;
    }

    @Override
    public String toString()
    {
        return "property "+property+" changed from "+oldVal +" to "+newVal + " in " + target;

    }

    @Override
    public boolean succeeded() {
        return true; //how can you fail to set a regular property? //maybe we could handle invocation target exceptions
    }
}
