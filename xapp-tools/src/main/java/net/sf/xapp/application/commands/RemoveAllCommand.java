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

import net.sf.xapp.application.api.MultiNodeCommand;
import net.sf.xapp.application.api.Node;

import java.util.List;

public class RemoveAllCommand extends MultiNodeCommand
{
    public RemoveAllCommand()
    {
        super("Remove ALL", "remove these nodes", "shift DELETE");
    }

    public void execute(List<Node> nodes)
    {
        for (Node node : nodes)
        {
            new RemoveCommand().execute(node);
        }
    }
}