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
import net.sf.xapp.application.core.Clipboard;

public class CutCommand extends NodeCommand
{
    public CutCommand()
    {
        super("Cut", "places node on clipboard, removed on paste", "control X");
    }

    public void execute(Node node)
    {
        node.getAppContainer().getClipboard().setAction(Clipboard.Action.CUT);
        node.getAppContainer().getClipboard().addClipboardObject(node.wrappedObject());
    }
}
