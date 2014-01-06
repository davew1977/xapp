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

import net.sf.xapp.application.api.Command;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.NodeCommand;
import net.sf.xapp.application.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PopUpCreateCommand extends NodeCommand
{
    private List<CreateCommand> m_createCommands;
    private Point popupLocation;
    private Component invoker;

    public PopUpCreateCommand(List<CreateCommand> createCommands, Point popupLocation, Component invoker)
    {
        super("Create...", "create a new object", "control N");
        m_createCommands = createCommands;
        this.popupLocation = popupLocation;
        this.invoker = invoker;
    }

    public void execute(final Node node)
    {
        JPopupMenu menu = new JPopupMenu();
        for (final Command command : m_createCommands)
        {
            JMenuItem menuItem = new JMenuItem(command.getName());
            menuItem.setFont(SwingUtils.DEFAULT_FONT);
            menuItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    command.execute(node);
                    node.getApplicationContainer().getMainFrame().repaint();
                }
            });
            menuItem.setToolTipText(command.getDescription());
            menu.add(menuItem);

        }
        if (invoker == null)
        {
            JTree tree = node.getApplicationContainer().getMainTree();
            int row = tree.getRowForPath(node.getPath());
            Rectangle rowBounds = tree.getRowBounds(row);
            int y = (int) rowBounds.getY();
            int x = (int) (rowBounds.getX()+rowBounds.getWidth());
            invoker = tree;
            popupLocation = new Point(x,y);
        }
//        System.out.println("y = " + y);
//        System.out.println("x = " + x);
//        System.out.println("row = " + row);
        menu.show(invoker,popupLocation.x, popupLocation.y);
    }
}
