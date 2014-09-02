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

import javax.swing.tree.TreePath;

public class MoveUpCommand extends NodeCommand
{
    public MoveUpCommand()
    {
        super("Move up", "moves this node up one place", "alt pressed UP");
    }

    public void execute(Node node)
    {
        //get the parent list node
        TreePath selectionPath = node.getAppContainer().getMainTree().getSelectionPath();
        node.getAppContainer().getNodeUpdateApi().moveInList(node.myObjLocation(), node.objectMeta(), -1);
        node.getAppContainer().getMainTree().setSelectionPath(selectionPath);
    }
}