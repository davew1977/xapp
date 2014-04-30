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
import net.sf.xapp.application.core.NodeBuilder;
import net.sf.xapp.application.editor.*;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.PropertyChangeTuple;

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
        final Object instance = m_createClass.newInstance();
        final ApplicationContainer applicationContainer = parentNode.getApplicationContainer();
        final ListNodeContext listNodeContext = parentNode.getListNodeContext();
        applicationContainer.getApplication().nodeAboutToBeAdded(listNodeContext.getContainerProperty(), listNodeContext.getListOwner(), instance);
        EditableContext editableContext = new SingleTargetEditableContext(m_createClass, instance, SingleTargetEditableContext.Mode.CREATE);
        final Editor defaultEditor = EditorManager.getInstance().getEditor(editableContext, new EditorAdaptor()
        {
            public void save(List<PropertyChangeTuple> changes, boolean closeOnSave)
            {
                if (!listNodeContext.contains(instance))
                {
                    listNodeContext.add(instance);
                    NodeBuilder nodeBuilder = applicationContainer.getNodeBuilder();
                    Node newNode = nodeBuilder.createNode(null, instance, parentNode, parentNode.getDomainTreeRoot(), ObjectNodeContext.ObjectContext.IN_LIST);
                    applicationContainer.getMainPanel().repaint();
                    parentNode.updateDomainTreeRoot();
                    applicationContainer.getApplication().nodeAdded(newNode);
                }
            }
        });
        defaultEditor.getMainFrame().setLocationRelativeTo(applicationContainer.getMainPanel());
        defaultEditor.getMainFrame().setVisible(true);
    }
}
