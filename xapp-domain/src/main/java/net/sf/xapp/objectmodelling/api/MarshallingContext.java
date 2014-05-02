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
package net.sf.xapp.objectmodelling.api;

import net.sf.xapp.objectmodelling.core.ClassModelManager;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.PropertyObjectPair;
import net.sf.xapp.objectmodelling.difftracking.KeyChangeHistory;

public interface MarshallingContext<T>
{
    void mapIncludedResourceURL(Object unmarshalledObj, String resourceURL);

    void mapIncludedResourceURLByReference(PropertyObjectPair propertyObjectPair, String resourceURL);

    String getIncludedResourceURL(Object unmarshalledObject);

    String getIncludedResourceURLByReference(PropertyObjectPair propertyObjectPair);

    ClassModelManager createChildCMM(Class[] retainList);

    KeyChangeHistory getKeyChangeHistory();

    /**
     *
     * @param obj the root datamodel object
     */
    void setInitialized(ObjectMeta<T> obj);
}
