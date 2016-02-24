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
package net.sf.xapp.application.api;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Allows an application to customise the Jtree cell rendering in a (hopefully) simpler way than by
 * overriding {@link DefaultTreeCellRenderer}
 *
 * NOTE: There are BIG problems when trying to provide a custom rendering that dynamically changes the
 * cell's size. One way to fix this is by triggering a node change event when change the cell size - but
 * be careful not to trigger unnecessary events. e.g:
 * <p><CODE>
 *     ((DefaultTreeModel)m_appContainer.getMainTree().getModel()).nodeChanged(node.getJtreeNode());
 * </CODE</p
 */
public interface SpecialTreeGraphics<T>
{
    /**
     * called at initialisation time
     * @param applicationContainer  a reference to the app container
     */
    void init(ApplicationContainer<T> applicationContainer);

    /**
     * get the special icon for the node, if any
     * @param node the node
     * @return return null to use default
     */
    ImageIcon getNodeImage(Node node);

    /**
     * Allows painting directly on top of the cell
     * @param node the node
     * @param g g
     */
    void decorateCell(Node node, Graphics2D g);

    /**
     * override the default tootip (which is print the values of all non transient properties)
     * @param node the node
     * @return the tooltip
     */
    String getTooltip(Node node);

    /**
     *
     * @param currentNode the node
     * @param cellRenderer the renderer
     */
    void prepareRenderer(Node currentNode, DefaultTreeCellRenderer cellRenderer);
}
