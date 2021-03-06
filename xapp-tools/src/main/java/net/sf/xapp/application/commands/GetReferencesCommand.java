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


import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.NodeCommand;
import net.sf.xapp.application.editor.EditorAdaptor;
import net.sf.xapp.application.editor.widgets.ListReferenceGUI;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.PropertyUpdate;

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
            public void save(List<PropertyUpdate> changes, boolean closing)
            {
                List newValues = gui.getData();
                ClassDatabase cdb = node.getClassDatabase();

                Collection oldValues = node.getListNodeContext().getCollection();
                List<Object> toRemove = new ArrayList<Object>(oldValues);
                List<Object> toAdd = new ArrayList<Object>(newValues);
                toRemove.removeAll(newValues);
                toAdd.removeAll(oldValues);
                ClassModel cm = node.getListNodeContext().getContainerProperty().getContainedTypeClassModel();
                List<ObjectMeta> refsToRemove = cm.findAll(toRemove);
                List<ObjectMeta> refsToAdd = cm.findAll(toAdd);
                boolean unchanged = toAdd.isEmpty() && toRemove.isEmpty();
                if(!unchanged) {
                    node.getAppContainer().getNodeUpdateApi().updateReferences(node.toObjLocation(), refsToAdd, refsToRemove);
                }
            }
        });
        gui.setLocationRelativeTo(node.getAppContainer().getMainPanel());
        gui.setVisible(true);
    }
}
