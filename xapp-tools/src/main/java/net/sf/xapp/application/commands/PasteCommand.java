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
import net.sf.xapp.application.api.NodeUpdateApi;
import net.sf.xapp.application.core.Clipboard;
import net.sf.xapp.objectmodelling.core.ClassModel;

import java.util.ArrayList;
import java.util.List;

public class PasteCommand extends NodeCommand
{

    public PasteCommand(Node node)
    {
        super(name(node), "paste clipboard object(s)", "control V");
    }

    private static String name(Node node)
    {
        int noOfObjects = node.getAppContainer().getClipboard().getClipboardObjects().size();
        String suffix = noOfObjects != 0 ? " (" + noOfObjects + " items)" : "";
        if(node.containsReferences())
        {
            suffix+=" references";
        }
        return "Paste" + suffix;
    }

    public void execute(Node node)
    {
        ApplicationContainer applicationContainer = node.getAppContainer();
        Clipboard clipboard = applicationContainer.getClipboard();
        List<Object> list = clipboard.getClipboardObjects();
        List<Object> clones = new ArrayList<Object>();
        NodeUpdateApi nodeUpdateApi = applicationContainer.getNodeUpdateApi();
        for (Object clipboardObject : list) {
            ClassModel classModel = applicationContainer.getClassDatabase().getClassModel(clipboardObject.getClass());

            if (!node.containsReferences()) //don't remove or clone if we're only pasting references
            {
                //remove if action was CUT
                if (clipboard.isCut()) {
                    nodeUpdateApi.moveObject(node, clipboardObject);
                } else {
                    nodeUpdateApi.insertObject(node, clipboardObject);
                }
                //if cloneable then create new clone for clipboard
                if (clipboardObject instanceof Cloneable) {
                    Object clone = classModel.createClone(clipboardObject);
                    clones.add(clone);
                }

                //register so we get new object meta
            } else {
                //create reference to obj
                nodeUpdateApi.createReference(node, clipboardObject);
            }
        }

        clipboard.setAction(clones.isEmpty() ? Clipboard.Action.CUT : Clipboard.Action.COPY);
        clipboard.setClipboardObjects(clones);
    }
}
