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

public class RefreshCommand extends NodeCommand
{
    public RefreshCommand()
    {
        super("Refresh", "refresh this node to show newly added sub-nodes", "control R");
    }

    public void execute(Node node)
    {
        node.getAppContainer().getNodeBuilder().refresh(node);
    }
}
