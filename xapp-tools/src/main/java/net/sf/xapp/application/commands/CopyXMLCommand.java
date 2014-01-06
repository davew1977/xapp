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
import net.sf.xapp.marshalling.Marshaller;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class CopyXMLCommand extends NodeCommand
{
    public CopyXMLCommand()
    {
        super("Copy XML", "Copies XML node to system clipboard", "control shift C");
    }

    public void execute(Node node)
    {
        Object obj = node.wrappedObject();
        Marshaller marshaller = node.getApplicationContainer().getGuiContext().getClassDatabase().createMarshaller(obj.getClass());
        String data = marshaller.toXMLString(obj);
        StringSelection s = new StringSelection(data);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, null);
    }
}
