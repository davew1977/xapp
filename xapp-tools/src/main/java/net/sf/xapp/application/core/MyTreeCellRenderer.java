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
package net.sf.xapp.application.core;

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.SpecialTreeGraphics;
import net.sf.xapp.application.api.ToolTipHandler;
import net.sf.xapp.tree.Tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;


public class MyTreeCellRenderer extends DefaultTreeCellRenderer
{

    private ToolTipHandler m_toolTipHandler;
    private SpecialTreeGraphics m_treeGraphics;
    private Node m_currentNode;
    protected JTree m_jTree;

    public MyTreeCellRenderer(ToolTipHandler toolTipHandler, JTree jTree)
    {
        m_toolTipHandler = toolTipHandler;
        m_jTree= jTree;
    }

    public void setTreeGraphics(SpecialTreeGraphics treeGraphics)
    {
        m_treeGraphics = treeGraphics;
    }

    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus)
    {

        super.getTreeCellRendererComponent(tree, value, sel,
                expanded, leaf, row,
                hasFocus);

        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
        Node node = (Node) treeNode.getUserObject();
        //do images
        setIcon(getIcon(node));


        //do tool tips
        if(m_treeGraphics!=null && m_treeGraphics.getTooltip(node)!=null)
        {
            setToolTipText(m_treeGraphics.getTooltip(node));
        }
        else
        {
            setToolTipText(m_toolTipHandler.getTooltip(node));
        }

        m_currentNode = node;


        if(m_treeGraphics!=null) m_treeGraphics.prepareRenderer(m_currentNode, this);

        return this;
    }

    public Icon getIcon(Node node)
    {
        if(m_treeGraphics!=null && m_treeGraphics.getNodeImage(node)!=null)
            return m_treeGraphics.getNodeImage(node);
        else if(node.isReference())
        {
            return ApplicationContainer.ASTERISK_ICON;
        }
        else if(node.wrappedObject() instanceof Tree)
        {
            Tree t = (Tree) node.wrappedObject();
            return ApplicationContainer.FOLDER_ICON;
        }
        else if(node.getListNodeContext()!=null)
            return ApplicationContainer.FOLDER_ICON;
        else
            return ApplicationContainer.OBJECT_ICON;
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        if(m_treeGraphics!=null) m_treeGraphics.decorateCell(m_currentNode, g);
    }

    public int getRowHeight()
    {
        return 16;
    }

}
