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
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.charset.Charset;

public class PasteXMLCommand extends NodeCommand
{
    public PasteXMLCommand()
    {
        super("Paste XML","try and unmarshal an object from clipboard content and paste it","control shift V");
    }

    public void execute(Node node)
    {
        ApplicationContainer applicationContainer = node.getAppContainer();
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

        String text = getTextFromClipboard(t);
        //read opening tag
        String className = text.substring(1, text.indexOf(' '));
        ClassDatabase cdb = applicationContainer.getGuiContext().getClassDatabase();
        ClassModel classModel = cdb.getClassModelBySimpleName(className);
        applicationContainer.getNodeUpdateApi().deserializeAndInsert(node, classModel, text);
    }

    private String getTextFromClipboard(Transferable t) {
        try {
            return (String) t.getTransferData(DataFlavor.stringFlavor);
        }
        catch (UnsupportedFlavorException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
