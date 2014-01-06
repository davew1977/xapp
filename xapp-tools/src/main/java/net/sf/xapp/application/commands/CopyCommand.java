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

public class CopyCommand extends NodeCommand
{
    public CopyCommand()
    {
        super("Copy", "Copies node to clipboard", "control C");
    }

    public void execute(Node node)
    {
        Object instance = node.wrappedObject();
        //assert instance instanceof Cloneable;
        Object clone = node.getObjectNodeContext().getClassModel().createClone(instance);
        node.getApplicationContainer().getClipboard().setAction(Clipboard.Action.COPY);
        node.getApplicationContainer().getClipboard().addClipboardObject(clone);
    }
}
