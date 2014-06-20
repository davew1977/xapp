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
package net.sf.xapp.application.commands;

import net.sf.xapp.application.api.*;
import net.sf.xapp.application.editor.*;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.PropertyUpdate;

import java.util.List;

public class CreateCommand extends NodeCommand
{
    private ClassModel m_createClass;

    public CreateCommand(ClassModel validImpl)
    {
        super("Create "+validImpl.getSimpleName(), "Creates new node in this list", "control N");
        m_createClass = validImpl;
    }

    public void execute(final Node parentNode)
    {
        final ApplicationContainer appContainer = parentNode.getAppContainer();
        final ObjectMeta objMeta = appContainer.getNodeUpdateApi().createObject(parentNode, m_createClass);
        EditableContext editableContext = new SingleTargetEditableContext(objMeta, SingleTargetEditableContext.Mode.CREATE, appContainer.getNodeUpdateApi());
        final Editor defaultEditor = EditorManager.getInstance().getEditor(editableContext, new EditorAdaptor()
        {
            public void save(List<PropertyUpdate> updates, boolean closeOnSave)
            {
                appContainer.getNodeUpdateApi().initObject(objMeta, updates);
            }

            @Override
            public void close() {
                appContainer.getNodeUpdateApi().deleteObject(objMeta);
            }
        });
        defaultEditor.getMainFrame().setLocationRelativeTo(appContainer.getMainPanel());
        defaultEditor.getMainFrame().setVisible(true);
    }
}
