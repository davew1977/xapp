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


import net.sf.xapp.application.api.ListNodeContext;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.NodeCommand;
import net.sf.xapp.application.editor.EditorAdaptor;
import net.sf.xapp.application.editor.widgets.ListReferenceGUI;
import net.sf.xapp.objectmodelling.core.ListProperty;
import net.sf.xapp.objectmodelling.core.PropertyChangeTuple;

import java.util.*;

public class GetReferencesCommand extends NodeCommand
{
    public GetReferencesCommand()
    {
        super("Select objects", "selects the objects to place in this list", "control shift E");
    }

    public void execute(final Node node)
    {
        final ListReferenceGUI gui = new ListReferenceGUI(node);
        gui.setGuiListener(new EditorAdaptor()
        {
            public void save(List<PropertyChangeTuple> changes, boolean closing)
            {
                ListNodeContext context = node.getListNodeContext();
                Collection list = context.getCollection();
                Collection before = new ArrayList(list);
                list.clear();
                list.addAll(gui.getData());
                ListProperty listProperty = context.getListProperty();
                listProperty.set(context.getListOwner(), list);
                Node updatedNode = node.getObjectNodeContext() != null ? node : node.getParent();
                Map<String, PropertyChangeTuple> map = new HashMap<String, PropertyChangeTuple>();
                map.put(listProperty.getName(), new PropertyChangeTuple(listProperty, updatedNode.wrappedObject(), before, list));
                node.getApplicationContainer().getNodeBuilder().refresh(node);
                if (!before.equals(list))
                {
                    node.getApplicationContainer().getApplication().nodeUpdated(updatedNode, map);
                }
            }
        });
        gui.setLocationRelativeTo(node.getApplicationContainer().getMainPanel());
        gui.setVisible(true);
    }
}