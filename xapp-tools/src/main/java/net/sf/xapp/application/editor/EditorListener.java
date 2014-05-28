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

import net.sf.xapp.objectmodelling.core.PropertyChange;
import net.sf.xapp.objectmodelling.core.PropertyChange;
import net.sf.xapp.objectmodelling.core.PropertyUpdate;

import java.util.List;

public interface EditorListener
{
    void save(List<PropertyUpdate> changes, boolean closeOnSave);

    void close();

    public static class NullEditorListener implements EditorListener
    {

        public void save(List<PropertyUpdate> changes, boolean closeOnSave)
        {

        }

        public void close()
        {

        }
    }
}
