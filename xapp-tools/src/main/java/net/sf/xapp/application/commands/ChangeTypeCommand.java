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
import net.sf.xapp.application.api.ListNodeContext;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.NodeCommand;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ChangeTypeCommand extends NodeCommand
{
    public ChangeTypeCommand()
    {
        super("Change Type", "change an object's type", "control Y");
    }

    public void execute(final Node  node)
    {
        JPopupMenu menu = new JPopupMenu();
        ListNodeContext parentListNodeContext = node.getParent().getListNodeContext();
        List<ClassModel> classOptions = parentListNodeContext.getValidImplementations();
        final List containingList = parentListNodeContext.getList();
        final ApplicationContainer appContainer = node.getAppContainer();
        for (final ClassModel targetClassModel : classOptions)
        {
            JMenuItem menuItem = new JMenuItem(targetClassModel.toString());
            menuItem.setFont(SwingUtils.DEFAULT_FONT);
            menuItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ClassModel srcClassModel = node.getObjectNodeContext().getClassModel();
                    ObjectMeta oldInstance = node.wrappedObject();
                    List<Property> properties = srcClassModel.getAllProperties();
                    ObjectMeta newInstance = targetClassModel.newInstance(node.getParent().objRef());
                    for (Property property : properties)
                    {
                        newInstance.set(property, oldInstance.get(property));
                    }
                    appContainer.getMainFrame().repaint();

                    //replace object in containing list
                    int index = containingList.indexOf(oldInstance);
                    containingList.remove(oldInstance);
                    containingList.add(index, newInstance);
                    //refresh so a new Node will be created, then we must select that node
                    //so that the whole operation is more transparent to the user
                    int oldNodeIndex = node.getParent().indexOf(node);
                    Node newNode = appContainer.getNodeBuilder().refresh(node.getParent());
                    appContainer.setSelectedNode(newNode.getChildAt(oldNodeIndex));
                }
            });
            menu.add(menuItem);

        }
        JTree tree = appContainer.getMainTree();
        int row = tree.getRowForPath(node.getPath());
        Rectangle rowBounds = tree.getRowBounds(row);
        int y = (int) rowBounds.getY();
        int x = (int) (rowBounds.getX()+rowBounds.getWidth());
//        System.out.println("y = " + y);
//        System.out.println("x = " + x);
//        System.out.println("row = " + row);
        menu.show(appContainer.getMainTree(),x,y);
    }
}
