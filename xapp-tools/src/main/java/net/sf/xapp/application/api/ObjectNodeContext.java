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
import net.sf.xapp.objectmodelling.core.Property;

/**
 * Encapsulates the properties of nodes that are backed with real objects in the data model.
 */
public interface ObjectNodeContext extends NodeContext
{

    boolean hasToStringMethod();

    Property getProperty();

    enum ObjectContext {
        IN_LIST,
        PROPERTY
    }

    ClassModel getClassModel();
    Object getInstance();
    ObjectContext getObjectContext();
    boolean canEdit();
}
