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

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.NodeCommand;
import net.sf.xapp.application.editor.*;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.PropertyChange;
import net.sf.xapp.objectmodelling.core.PropertyChange;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditCommand extends NodeCommand
{
    public EditCommand()
    {
        super("Edit", "opens editor for this object", "control E");
    }

    public void execute(final Node node)
    {
        //open edit defaultGui
        final ApplicationContainer appContainer = node.getApplicationContainer();
        final ObjectMeta objectToEdit = node.objectMeta();
        EditableContext editableContext = new SingleTargetEditableContext(
                objectToEdit, SingleTargetEditableContext.Mode.EDIT);
        Editor editor = EditorManager.getInstance().getEditor(editableContext, new EditorAdaptor()
        {
            public void save(List<PropertyChange> changes, boolean closing)
            {
                Node newNode = node;
                if (closing)
                {
                    newNode = appContainer.getNodeBuilder().refresh(node);
                    if(newNode.isReference())
                    {
                        Node referee = appContainer.getNode(newNode.wrappedObject());
                        referee.updateDomainTreeRoot();
                    }
                    else
                    {
                        newNode.updateDomainTreeRoot();
                    }
                }

                Map<String, PropertyChange> changeMap = new HashMap<String, PropertyChange>();
                for (PropertyChange change : changes)
                {
                    changeMap.put(change.property.getName(), change);
                }
                appContainer.getApplication().nodeUpdated(newNode, changeMap);
            }
        });

        if (editor.getMainFrame().getLocation().equals(new Point(0,0))) //only relocate gui if user has not in past
        {
            editor.getMainFrame().setLocationRelativeTo(appContainer.getMainPanel());
        }
        editor.getMainFrame().setVisible(true);
    }
}
