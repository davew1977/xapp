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

import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.difftracking.KeyChangeDictionary;

/**
 * Encapsulates services required by a {@link net.sf.xapp.objectmodelling.core.ClassModel}
 */
public interface ClassModelContext
{

    KeyChangeDictionary getKeyChangeDictionary();

    Object resolveInstance(ClassModel classModel, String key);

    boolean isInitializing();
}
