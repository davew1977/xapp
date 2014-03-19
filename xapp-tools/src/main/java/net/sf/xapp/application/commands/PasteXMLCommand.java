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
import net.sf.xapp.objectmodelling.core.ClassModel;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.nio.charset.Charset;

public class PasteXMLCommand extends NodeCommand
{
    public PasteXMLCommand()
    {
        super("Paste XML","try and unmarshal an object from clipboard content and paste it","control shift V");
    }

    public void execute(Node node)
    {
        ApplicationContainer applicationContainer = node.getApplicationContainer();
        ClassModel containedTypeClassModel = node.getListNodeContext().getListProperty().getContainedTypeClassModel();

        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        Object clipboardObject = null;

        try
        {
            String text = (String) t.getTransferData(DataFlavor.stringFlavor);
            //read opening tag
            String className = text.substring(1, text.indexOf(' '));
            Unmarshaller unmarshaller = applicationContainer.getGuiContext().getClassDatabase().createUnmarshaller(className);
            clipboardObject = unmarshaller.unmarshalString(text, Charset.forName("UTF-8"));
        }
        catch (Exception e)
        {
            System.out.println("WARNING: could not paste XML from system clipboard: "+e.getMessage());
            return;
        }


        if (!containedTypeClassModel.isInstance(clipboardObject))
        {
            System.out.println("WARNING: cannot paste a " + clipboardObject.getClass().getSimpleName() + " here!");
            return;
        }

        //add object to data model
        node.getListNodeContext().getCollection().add(clipboardObject);
        //create new node
        Node newNode = applicationContainer.getNodeBuilder().createNode(null, clipboardObject, node, node.getDomainTreeRoot(), null);

        applicationContainer.getMainPanel().repaint();
        node.updateDomainTreeRoot();
        applicationContainer.getApplication().nodeAdded(newNode);
    }
}
