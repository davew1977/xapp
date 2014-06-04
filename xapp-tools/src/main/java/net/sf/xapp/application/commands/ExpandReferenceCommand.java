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

public class ExpandReferenceCommand extends NodeCommand
{
    public ExpandReferenceCommand()
    {
        super("Expand Reference", "This node is a only a reference to another, click here to goto the main node", "control U");
    }

    public void execute(final Node node)
    {
        assert node.isReference();
        ApplicationContainer ac = node.getAppContainer();
        ac.collapseAll();
        ac.expand(node);
        ac.expand(node.wrappedObject());
    }
}