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

import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
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
        ObjectMeta objectMeta = m_applicationContainer.getGuiContext().getObjectMeta();
        Node node = createNode(objectMeta.getHome(), objectMeta);
        m_applicationContainer.getMainTree().setModel(new DefaultTreeModel(node.getJtreeNode()));
        return node;
    }

    public Node createNode(ObjectLocation objectLocation, ObjectMeta objMeta) {
        return createNode(objectLocation, objMeta, -1);
    }
    public Node createNode(ObjectLocation objectLocation, ObjectMeta objMeta, int insertIndex)
    {
        Property parentProperty = objectLocation.getProperty();
        if(insertIndex==-1) {
            insertIndex = parentNode.numChildren();
        }
        DefaultMutableTreeNode jtreeNode = new DefaultMutableTreeNode();
        DefaultTreeModel treeModel = (DefaultTreeModel) m_applicationContainer.getMainTree().getModel();

        ClassModel classModel = objMeta.getClassModel();
        ClassDatabase cdb = classModel.getClassDatabase();
        ObjectNodeContext objectNodeContext = new ObjectNodeContextImpl(parentProperty, classModel, objMeta,
                objectLocation.isCollection() ? IN_LIST : PROPERTY);
        ListNodeContextImpl listNodeContext = null;
        ListProperty containerListProperty = classModel.getContainerListProperty();
        if (classModel.isContainer())
        {
            ListProperty listProp = containerListProperty;
            ObjectMeta listOwner = objMeta;
            listNodeContext = new ListNodeContextImpl(listProp, listOwner);
        }
        Node newNode = new NodeImpl(m_applicationContainer, jtreeNode, listNodeContext, objectNodeContext);
        objMeta.setAttachment(newNode);

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
        List<ContainerProperty> containerProperties =  new ArrayList<ContainerProperty>(classModel.getMapProperties());
        List<ListProperty> listProperties = classModel.getListProperties();
        containerProperties.addAll(listProperties);
        for (ContainerProperty listProperty : containerProperties)
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
            if (cdb.getStringSerializer(property.getPropertyClass()) != null) continue;

            Object value = objMeta.get(property);
            //register the obj if the obj meta is null
            ObjectMeta propValueObjMeta = null;
            ObjectLocation newLocation = new ObjectLocation(objMeta, property);
            newLocation.setAttachment(newNode);
            if (value != null) {
                propValueObjMeta = cdb.findOrCreateObjMeta(newLocation, value);
            }
            if (value != null && propValueObjMeta != null)
            {
                createNode(newLocation, propValueObjMeta);
            }
        }
        return newNode;
    }

    private Node createListNode(ContainerProperty listProperty, Node parentNode, int insertIndex)
    {
        ObjectMeta listOwner = parentNode.objectMeta();
        DefaultMutableTreeNode parentJTreeNode = parentNode.getJtreeNode();
        DefaultTreeModel treeModel = (DefaultTreeModel) m_applicationContainer.getMainTree().getModel();
        ListNodeContextImpl lnc = new ListNodeContextImpl(listProperty, listOwner);
        DefaultMutableTreeNode jListNode = new DefaultMutableTreeNode();
        Node listNode = new NodeImpl(m_applicationContainer, jListNode, lnc, null);
        jListNode.setUserObject(listNode);
        treeModel.insertNodeInto(jListNode, parentJTreeNode, insertIndex);
        populateListNodes(listNode);
        return listNode;
    }

    public void populateListNodes(Node parentNode)
    {
        ClassDatabase cdb = parentNode.getClassDatabase();
        ListNodeContext listNodeContext = parentNode.getListNodeContext();
        Collection list = listNodeContext.getCollection();
        for (Object o : list)
        {
            ObjectLocation objectLocation = parentNode.newObjLocation();
            createNode(objectLocation, cdb.findOrCreateObjMeta(objectLocation, o));
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
            ObjectLocation objectLocation = node.thisObjLocation();
            int oldIndex = parent.indexOf(node);
            m_applicationContainer.removeNode(node, true);
            if(node.getObjectNodeContext()!=null)
            {
                newNode = createNode(objectLocation, node.objectMeta(), oldIndex);
            }
            else
            {
                newNode = createListNode(node.getListNodeContext().getContainerProperty(), parent, oldIndex);
            }
        }
        m_applicationContainer.getMainPanel().repaint();
        return newNode;
    }
}
