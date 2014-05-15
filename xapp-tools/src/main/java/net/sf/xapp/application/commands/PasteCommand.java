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
import net.sf.xapp.application.api.ObjectNodeContext;
import net.sf.xapp.application.core.Clipboard;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PasteCommand extends NodeCommand
{

    public PasteCommand(Node node)
    {
        super(name(node), "paste clipboard object(s)", "control V");
    }

    private static String name(Node node)
    {
        int noOfObjects = node.getApplicationContainer().getClipboard().getClipboardObjects().size();
        String suffix = noOfObjects != 0 ? " (" + noOfObjects + " items)" : "";
        if(node.containsReferences())
        {
            suffix+=" references";
        }
        return "Paste" + suffix;
    }

    public void execute(Node node)
    {
        ApplicationContainer applicationContainer = node.getApplicationContainer();
        Clipboard clipboard = applicationContainer.getClipboard();
        List<Object> list = clipboard.getClipboardObjects();
        List<Object> clones = new ArrayList<Object>();
        for (Object clipboardObject : list) {
            ClassModel classModel = applicationContainer.getGuiContext().getClassDatabase().getClassModel(clipboardObject.getClass());

            ObjectMeta newObjMeta = null;
            if (!node.containsReferences()) //don't remove or clone if we're only pasting references
            {
                //remove if action was CUT
                if (clipboard.getAction() == Clipboard.Action.CUT) {
                    //remove from data model
                    Node clipboardNode = applicationContainer.getNode(clipboardObject);
                    Node parentNode = clipboardNode.getParent();
                    parentNode.getListNodeContext().getCollection().remove(clipboardObject);
                    //remove from object meta model (dispose, not delete because we don't want to delete any child objects
                    classModel.dispose(clipboardObject);
                    //remove the node
                    applicationContainer.removeNode(clipboardNode);

                    applicationContainer.getApplication().nodeRemoved(clipboardNode, true);
                }
                //if cloneable then create new clone for clipboard
                if (clipboardObject instanceof Cloneable) {
                    Object clone = classModel.createClone(clipboardObject);
                    clones.add(clone);
                }

                //register so we get new object meta
                newObjMeta = classModel.registerInstance(node.objectMeta(), clipboardObject);
                applicationContainer.getApplication().nodeAboutToBeAdded(
                        node.getListNodeContext().getContainerProperty(),
                        node.getListNodeContext().getListOwner(), clipboardObject);
            } else {
                newObjMeta = classModel.find(clipboardObject);
            }
            //add object to data model
            node.getListNodeContext().add(newObjMeta);
            //create new node
            Node newNode = applicationContainer.getNodeBuilder().createNode(null,
                    newObjMeta, node, node.getDomainTreeRoot(), ObjectNodeContext.ObjectContext.IN_LIST);

            applicationContainer.getMainPanel().repaint();
            node.updateDomainTreeRoot();
            applicationContainer.getApplication().nodeAdded(newNode);
        }

        clipboard.setAction(clones.isEmpty() ? Clipboard.Action.CUT : Clipboard.Action.COPY);
        clipboard.setClipboardObjects(clones);
    }
}
