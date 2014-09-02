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
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.util.ArrayList;
import java.util.Arrays;
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
        List<ObjectMeta> list = clipboard.getClipboardObjects();
        List<ObjectMeta> clones = new ArrayList<ObjectMeta>();
        NodeUpdateApi nodeUpdateApi = applicationContainer.getNodeUpdateApi();
        ObjectLocation objectLocation = node.toObjLocation();
        for (ObjectMeta clipboardObject : list) {

            if (!node.containsReferences()) //don't remove or clone if we're only pasting references
            {
                //remove if action was CUT
                nodeUpdateApi.moveOrInsertObjMeta(objectLocation, clipboardObject);
                //if cloneable then create new clone for clipboard
                if (clipboardObject.isCopyable()) {
                    clones.add(clipboardObject.copy());
                }
            } else {
                //create reference to obj
                nodeUpdateApi.updateReferences(node.toObjLocation(), Arrays.asList(clipboardObject), new ArrayList<ObjectMeta>());
            }
        }

        clipboard.setAction(clones.isEmpty() ? Clipboard.Action.CUT : Clipboard.Action.COPY);
        clipboard.setClipboardObjects(clones);
    }
}
