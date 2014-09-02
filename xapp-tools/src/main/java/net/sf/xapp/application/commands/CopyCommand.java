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
import net.sf.xapp.objectmodelling.core.ObjectMeta;

public class CopyCommand extends NodeCommand
{
    public CopyCommand()
    {
        super("Copy", "Copies node to clipboard", "control C");
    }

    public void execute(Node node)
    {
        ObjectMeta objectMeta= node.objectMeta();
        //assert instance instanceof Cloneable;
        node.getAppContainer().getClipboard().setAction(Clipboard.Action.COPY);
        node.getAppContainer().getClipboard().addClipboardObject(objectMeta.copy());
    }
}
