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
    private ApplicationContainerImpl appContainer;
    private ClassDatabase cdb;

    public NodeBuilder(ApplicationContainerImpl appContainer)
    {
        this.appContainer = appContainer;
        cdb = this.appContainer.getClassDatabase();
    }

    public Node createTree()
    {
        ObjectMeta objectMeta = appContainer.getGuiContext().getObjectMeta();
        Node node = createNode(null, objectMeta);
        appContainer.getMainTree().setModel(new DefaultTreeModel(node.getJtreeNode()));
        return node;
    }

    public Node createNode(Node parentNode, ObjectMeta objMeta) {
        return createNode(parentNode, objMeta, parentNode.numChildren());
    }
    public Node createNode(Node parentNode, ObjectMeta objMeta, int insertIndex)
    {
        //parent node may be:
        // 1) a list node containing objects
        // 2) a list node containing references
        // 3) an object node containing a set of objects (if it is a "container" class)
        // 4) an object node containing one or more complex objects (ones important enough to have their own nodes)
        Property parentProperty = objectLocation.getProperty();
        DefaultMutableTreeNode jTreeNode = new DefaultMutableTreeNode();

        ObjectContent

        ClassModel classModel = objMeta.getClassModel();
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
        Node newNode = new NodeImpl(appContainer, jTreeNode, listNodeContext, objectNodeContext);

        jTreeNode.setUserObject(newNode);
        if (!appContainer.getNodeFilter().accept(newNode))
        {
            //This user object is filtered out
            return newNode;
        }
        if (parentNode != null) //if not root
        {
            treeModel().insertNodeInto(jTreeNode, parentNode.getJtreeNode(), insertIndex);
        }
        //Short circuit here if the new node is but a reference
        if(newNode.isReference())
        {
            objMeta.attach(newNode.thisObjLocation(), newNode);
            return newNode;
        }
        objMeta.attach(newNode);

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
            if (value != null) {
                propValueObjMeta = cdb.findOrCreateObjMeta(new ObjectLocation(objMeta, property), value);
            }
            if (value != null && propValueObjMeta != null)
            {
                createNode(parentNode, propValueObjMeta);
            }
        }
        return newNode;
    }

    private DefaultTreeModel treeModel() {
        return (DefaultTreeModel) appContainer.getMainTree().getModel();
    }

    private Node createListNode(ContainerProperty listProperty, Node parentNode, int insertIndex)
    {
        ObjectMeta listOwner = parentNode.objectMeta();
        DefaultMutableTreeNode parentJTreeNode = parentNode.getJtreeNode();
        DefaultTreeModel treeModel = (DefaultTreeModel) appContainer.getMainTree().getModel();
        ListNodeContextImpl lnc = new ListNodeContextImpl(listProperty, listOwner);
        DefaultMutableTreeNode jListNode = new DefaultMutableTreeNode();
        Node listNode = new NodeImpl(appContainer, jListNode, lnc, null);
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
            ObjectLocation objectLocation = parentNode.asObjLocation();
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
            appContainer.removeNode(node, true);
            if(node.getObjectNodeContext()!=null)
            {
                newNode = createNode(objectLocation, node.objectMeta(), oldIndex);
            }
            else
            {
                newNode = createListNode(node.getListNodeContext().getContainerProperty(), parent, oldIndex);
            }
        }
        appContainer.getMainPanel().repaint();
        return newNode;
    }
}
