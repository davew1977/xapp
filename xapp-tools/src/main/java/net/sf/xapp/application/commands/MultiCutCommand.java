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
import net.sf.xapp.application.core.Clipboard;

import java.util.List;

public class MultiCutCommand extends MultiNodeCommand
{
    public MultiCutCommand(List<Node> nodes)
    {
        super(name(nodes), "places nodes on clipboard, removed on paste", "control X");
    }

    private static String name(List<Node> nodes)
    {
        return "Cut " + nodes.size() + " items";
    }

    public void execute(List<Node> params)
    {
        Clipboard clipboard = params.get(0).getAppContainer().getClipboard();
        clipboard.setAction(Clipboard.Action.CUT);
        for (Node node : params)
        {
            clipboard.addClipboardObject(node.objectMeta());
        }
    }
}