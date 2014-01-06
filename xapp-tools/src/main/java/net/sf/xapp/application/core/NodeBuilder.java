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

import net.sf.xapp.application.api.ListNodeContext;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.api.ObjectNodeContext;
import static net.sf.xapp.application.api.ObjectNodeContext.ObjectContext.IN_LIST;
import static net.sf.xapp.application.api.ObjectNodeContext.ObjectContext.PROPERTY;
import net.sf.xapp.annotations.objectmodelling.TreeMeta;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ListProperty;
import net.sf.xapp.objectmodelling.core.Property;
import net.sf.xapp.tree.Tree;
import net.sf.xapp.utils.XappException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.Collection;
import java.util.List;

/**
 * Creates the JTree nodes according to the model
 */
public class NodeBuilder
{
    private ApplicationContainerImpl m_applicationContainer;

    public NodeBuilder(ApplicationContainerImpl applicationContainer)
    {
        m_applicationContainer = applicationContainer;
    }

    public Node createTree()
    {
        Node node = createNode(m_applicationContainer.getGuiContext().getInstance(), null, null, null, 0);
        m_applicationContainer.getMainTree().setModel(new DefaultTreeModel(node.getJtreeNode()));
        return node;
    }

    public Node createNode(Object instance, Node parentNode, Tree domainTreeRoot, ObjectNodeContext.ObjectContext objectContext)
    {
        return createNode(instance, parentNode, domainTreeRoot, objectContext, parentNode.numChildren());
    }

    public Node createNode(Object instance, Node parentNode, Tree domainTreeRoot, ObjectNodeContext.ObjectContext objectContext, int insertIndex)
    {
        DefaultMutableTreeNode jtreeNode = new DefaultMutableTreeNode();
        DefaultTreeModel treeModel = (DefaultTreeModel) m_applicationContainer.getMainTree().getModel();

        ClassDatabase classDatabase = m_applicationContainer.getGuiContext().getClassDatabase();
        ClassModel classModel = classDatabase.getClassModel(instance.getClass());
        ObjectNodeContext objectNodeContext = new ObjectNodeContextImpl(classModel, instance, objectContext);
        ListNodeContextImpl listNodeContext = null;
        ListProperty containerListProperty = classModel.getContainerListProperty();
        if (classModel.isContainer())
        {
            ListProperty listProp = containerListProperty;
            Object listOwner = instance;
            listNodeContext = new ListNodeContextImpl(listProp, listOwner);
        }
        Node newNode = new NodeImpl(m_applicationContainer, domainTreeRoot, jtreeNode, listNodeContext, objectNodeContext);
        if(listNodeContext!=null)
        {
            listNodeContext.setNode(newNode);
        }
        jtreeNode.setUserObject(newNode);
        if (!m_applicationContainer.getNodeFilter().accept(newNode))
        {
            //This user object is filtered out
            return newNode;
        }
        if (parentNode != null) //if not root
        {
            treeModel.insertNodeInto(jtreeNode, parentNode.getJtreeNode(), insertIndex);
        }
        //Short circuit here if the new node is but a reference
        if(newNode.isReference())
        {
            return newNode;
        }
        //create child nodes for "container list prop"
        if (classModel.isContainer())
        {
            populateListNodes(newNode);
        }
        //create child nodes for lists
        List<ListProperty> listProperties = classModel.getListProperties();
        for (ListProperty listProperty : listProperties)
        {
            if (!listProperty.isDisplayNodes()) continue;
            if (!listProperty.isVisibilityRestricted()) continue;
            if (listProperty.getContainedType() == String.class) continue; //skip string lists
            if (listProperty.equals(classModel.getContainerListProperty())) continue;

            createListNode(listProperty, newNode, newNode.numChildren());
        }
        //create child nodes for relevant properties
        List<Property> properties = classModel.getProperties();
        for (Property property : properties)
        {
            //skip enums
            if (property.isEnum()) continue;
            //skip strings and primitives
            if (property.isSimpleType()) continue;
            //skip references
            if (property.isReference()) continue;
            //skip transients that are not deliberately set to "displayNodes"
            if (!property.isDisplayNodes()) continue;
            //skip if it has a bound property gui
            if (property.hasSpecialBoundComponent()) continue;
            //skip if has been made invisible
            if (!property.isVisibilityRestricted()) continue;
            //skip if there is a string serializer for this property
            if (property.getClassDatabase().getStringSerializer(property.getPropertyClass()) != null) continue;

            TreeMeta propertySubTreeMeta = property.getTreeMeta();
            if (domainTreeRoot != null && propertySubTreeMeta != null)
                throw new XappException("cannot have nested tree properties " + property);

            Object value = property.get(instance);
            if (value != null)
            {
                createNode(value, newNode, propertySubTreeMeta!=null ? (Tree) value : domainTreeRoot, PROPERTY);
            }
        }
        return newNode;
    }

    private Node createListNode(ListProperty listProperty, Node parentNode, int insertIndex)
    {
        Object listOwner = parentNode.wrappedObject();
        Tree domainTreeRoot = parentNode.getDomainTreeRoot();
        DefaultMutableTreeNode parentJTreeNode = parentNode.getJtreeNode();
        DefaultTreeModel treeModel = (DefaultTreeModel) m_applicationContainer.getMainTree().getModel();
        ListNodeContextImpl lnc = new ListNodeContextImpl(listProperty, listOwner);
        DefaultMutableTreeNode jListNode = new DefaultMutableTreeNode();
        Node listNode = new NodeImpl(m_applicationContainer, domainTreeRoot, jListNode, lnc, null);
        lnc.setNode(listNode);
        jListNode.setUserObject(listNode);
        treeModel.insertNodeInto(jListNode, parentJTreeNode, insertIndex);
        populateListNodes(listNode);
        return listNode;
    }

    public void populateListNodes(Node parentNode)
    {
        ListNodeContext listNodeContext = parentNode.getListNodeContext();
        Collection list = listNodeContext.getCollection();
        if (list != null)
        {
            for (Object o : list)
            {
                createNode(o, parentNode, parentNode.getDomainTreeRoot(), IN_LIST);
            }
        }
    }

    public Node refresh(Node node)
    {
        Node newNode = null;
        if(node.isRoot())
        {
            newNode = createTree();
        }
        else
        {
            Node parent = node.getParent();
            int oldIndex = parent.indexOf(node);
            m_applicationContainer.removeNode(node);
            if(node.getObjectNodeContext()!=null)
            {
                newNode = createNode(node.wrappedObject(), parent, node.getDomainTreeRoot(), node.getObjectNodeContext().getObjectContext(), oldIndex);
            }
            else
            {
                newNode = createListNode(node.getListNodeContext().getListProperty(), parent, oldIndex);
            }
        }
        m_applicationContainer.getMainPanel().repaint();
        return newNode;
    }
}
