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
import net.sf.xapp.application.api.MultiNodeCommand;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.editor.*;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.PropertyChange;
import net.sf.xapp.objectmodelling.core.PropertyChange;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

public class MultiEditCommand extends MultiNodeCommand
{
    private ApplicationContainer m_applicationContainer;
    private ClassModel m_commonClassModel;
    private List<Node> m_nodes;

    public MultiEditCommand(ApplicationContainer applicationContainer, List<Node> nodes, ClassModel commonClassModel)
    {
        super("Edit "+nodes.size()+ " items", "edit these nodes", "ctrl E");
        m_applicationContainer = applicationContainer;
        m_commonClassModel = commonClassModel;
        m_nodes = nodes;
    }

    public void execute(List<Node> nodes)
    {
        List<ObjectMeta> targets = extractTargets();
        EditableContext editableContext = new MultiTargetEditableContext(m_commonClassModel, targets);
        Editor editor = EditorManager.getInstance().getEditor(editableContext, new EditorAdaptor()
        {
            public void save(List<PropertyChange> changes, boolean closing)
            {
                if(changes.isEmpty())
                {
                    return; //nothing was changed
                }
                List<TreePath> newSelectionPaths = new ArrayList<TreePath>();
                for (Node node : m_nodes)
                {
                    Node newNode = m_applicationContainer.getNodeBuilder().refresh(node);
                    newSelectionPaths.add(newNode.getPath());
                    newNode.updateDomainTreeRoot();                  
                }
                m_applicationContainer.getApplication().nodesUpdated(changes);
                m_applicationContainer.getMainTree().setSelectionPaths(newSelectionPaths.toArray(new TreePath[newSelectionPaths.size()]));
            }
        });
        editor.getMainFrame().setLocationRelativeTo(m_applicationContainer.getMainPanel());
        editor.getMainFrame().setVisible(true);
    }

    private List<ObjectMeta> extractTargets()
    {
        ArrayList<ObjectMeta> targets = new ArrayList<ObjectMeta>();
        for (Node node : m_nodes)
        {
            targets.add(node.objectMeta());
        }
        return targets;
    }
}
