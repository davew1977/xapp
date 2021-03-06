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
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

public class ChangeTypeCommand extends NodeCommand
{
    public ChangeTypeCommand()
    {
        super("Change Type", "change an object's type", "control Y");
    }

    public void execute(final Node  node)
    {
        JPopupMenu menu = new JPopupMenu();
        Set<ClassModel> classOptions = node.objectMeta().compatibleTypes();
        final ApplicationContainer appContainer = node.getAppContainer();
        for (final ClassModel targetClassModel : classOptions)
        {
            JMenuItem menuItem = new JMenuItem(targetClassModel.toString());
            menuItem.setFont(SwingUtils.DEFAULT_FONT);
            menuItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    appContainer.getNodeUpdateApi().changeType(node.objectMeta(), targetClassModel);
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
