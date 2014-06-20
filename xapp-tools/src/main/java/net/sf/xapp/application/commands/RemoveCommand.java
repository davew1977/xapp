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
import net.sf.xapp.application.api.NodeUpdateApi;

public class RemoveCommand extends NodeCommand
{

    public RemoveCommand()
    {
        super("Remove", "remove this node", "shift DELETE");
    }

    public void execute(Node node)
    {
        ApplicationContainer appContainer = node.getAppContainer();
        if(node.isRoot())
        {
            System.out.println("cannot remove root!");
            return;
        }
        Node parentNode =  node.getParent();


        NodeUpdateApi nodeUpdateApi = appContainer.getNodeUpdateApi();
        Node childBefore = parentNode.getChildBefore(node);

        if(node.isReference()) {
            nodeUpdateApi.removeReference(node);
        } else {
            nodeUpdateApi.deleteObject(node);
        }

        //we want the selection path to be on above the node removed or the parent path if this does not exist
        appContainer.setSelectedNode(childBefore != null ? childBefore : parentNode);
    }
}
