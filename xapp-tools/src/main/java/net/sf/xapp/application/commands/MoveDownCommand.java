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

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.List;

public class MoveDownCommand extends NodeCommand
{
    public MoveDownCommand()
    {
        super("Move down", "moves this node down one place", "alt pressed DOWN");
    }

    public void execute(Node node)
    {
        //get the parent list node
        Node parentNode = node.getParent();
        TreePath selectionPath = node.getApplicationContainer().getMainTree().getSelectionPath();
        //move up the object in the real model
        List list = parentNode.getListNodeContext().getList();
        Object wrappedObject = node.wrappedObject();
        int i = list.indexOf(wrappedObject);
        if (i == list.size() - 1) return; //cannot move further down!
        list.remove(i);
        int newIndex = i + 1;
        list.add(newIndex, wrappedObject);
        //do to tree model
        DefaultTreeModel treeModel = (DefaultTreeModel) node.getApplicationContainer().getMainTree().getModel();
        treeModel.removeNodeFromParent(node.getJtreeNode());
        treeModel.insertNodeInto(node.getJtreeNode(), parentNode.getJtreeNode(), newIndex);
        node.getApplicationContainer().getMainTree().setSelectionPath(selectionPath);
        node.getApplicationContainer().getApplication().nodeMovedDown(node);
    }
}