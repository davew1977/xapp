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
package net.sf.xapp.application.api;

import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ListProperty;

import java.util.Collection;
import java.util.List;

/**
 * Encapsulates the properties of nodes that are backed either by container objects or simple lists in the
 * data model.
 */
public interface ListNodeContext extends NodeContext
{
    /**
     * @return the actual collection object from the data model
     */
    Collection getCollection();
    List getList();

    /**
     * @return meta data about the actual list property from the data model
     */
    ListProperty getListProperty();

    /**
     *
     * @return the actual object from the data model that has this list as a property
     */
    Object getListOwner();

    /**
     * @return list of types that are allowed to be placed in the list
     */
    List<ClassModel> getValidImplementations();
}
