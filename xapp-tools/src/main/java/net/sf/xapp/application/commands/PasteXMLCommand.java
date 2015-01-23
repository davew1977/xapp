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
import net.sf.xapp.application.core.ListNodeContext;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.utils.Filter;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

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

        String xml = getTextFromClipboard(t);
        //read opening tag
        String className = xml.substring(1, xml.indexOf(' '));
        ClassDatabase cdb = applicationContainer.getGuiContext().getClassDatabase();
        ClassModel classModel = cdb.getClassModelBySimpleName(className);

        //default to inserting into appropriate list nodes
        ListNodeContext listNodeContext = node.getListNodeContext();
        final Class containedClass = classModel.getContainedClass();
        if(listNodeContext != null && listNodeContext.getContainerProperty().getContainedType().isAssignableFrom(containedClass)) {
            applicationContainer.getNodeUpdateApi().deserializeAndInsert(node.toObjLocation(), classModel, xml, Charset.defaultCharset());
        } else {
            ObjectMeta objectMeta = node.objectMeta();
            java.util.List<Property> matchingProps = objectMeta.getClassModel().getAllProperties(new Filter<Property>() {
                @Override
                public boolean matches(Property property) {
                    return property.getMainType().isAssignableFrom(containedClass) && !property.isCollection();
                }
            });
            if(matchingProps.size()==1) {
                //TODO handle case where the property already has a value
                applicationContainer.getNodeUpdateApi().deserializeAndInsert(new ObjectLocation(objectMeta, matchingProps.get(0)), classModel, xml, Charset.defaultCharset());

            } else if(matchingProps.isEmpty()) {
                SwingUtils.warnUser(applicationContainer.getMainFrame(), "nowhere found to paste the object");
            } else {
                SwingUtils.warnUser(applicationContainer.getMainFrame(), "more that one place to paste, too ambiguous");

            }
        }

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
