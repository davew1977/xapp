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

import net.sf.xapp.application.api.Node;


import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.*;

import javax.swing.tree.DefaultTreeModel;
import java.util.Collection;
import java.util.List;

/**
 * Creates the JTree nodes according to the model
 */
public class NodeBuilder {
    private ApplicationContainerImpl appContainer;
    private ClassDatabase cdb;

    public NodeBuilder(ApplicationContainerImpl appContainer) {
        this.appContainer = appContainer;
        cdb = this.appContainer.getClassDatabase();
    }

    public Node createTree() {
        ObjectMeta objectMeta = appContainer.getGuiContext().getObjectMeta();
        Node node = createNode(null, objectMeta, 0);
        appContainer.getMainTree().setModel(new DefaultTreeModel(node.getJtreeNode()));
        return node;
    }

    public Node createNode(Node parentNode, ObjectMeta objMeta) {
        return createNode(parentNode, objMeta, parentNode.numChildren());
    }

    public Node createNode(Node parentNode, ObjectMeta objMeta, int insertIndex) {
        if (!appContainer.getNodeFilter().accept(objMeta)) {
            return null;
        }
        //parent node may be:
        // 1) a list node containing objects
        // 2) a list node containing references
        // 3) an object node containing a set of objects (if it is a "container" class)
        // 4) an object node containing one or more complex objects (ones important enough to have their own nodes)
        Node newNode = new NodeImpl(appContainer, parentNode, insertIndex, objMeta);
        //if node is not a reference then populate the chilren
        if (!newNode.isReference()) {
            //create child nodes for "container list prop"
            if (objMeta.isContainer()) {
                populateListNodes(newNode);
            }
            //create child nodes for lists
            List<ContainerProperty> list = objMeta.allContainerProps();
            for (ContainerProperty listProperty : list) {
                if (!listProperty.isDisplayNodes()) continue;
                if (!listProperty.isVisibilityRestricted()) continue;
                if (listProperty.getContainedType() == String.class) continue; //skip string lists
                if (listProperty.equals(objMeta.getContainerProperty())) continue;

                createListNode(newNode, listProperty, newNode.numChildren());
            }
            //create child nodes for relevant properties
            List<Property> properties = objMeta.getProperties();
            for (Property property : properties) {
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
                if (value != null && propValueObjMeta != null) {
                    createNode(parentNode, propValueObjMeta);
                }
            }
        }

        return newNode;
    }

    private Node createListNode(Node parentNode, ContainerProperty listProperty, int insertIndex) {
        ObjectMeta listOwner = parentNode.objectMeta();
        Node listNode = new NodeImpl(appContainer,parentNode, insertIndex, new ObjectLocation(listOwner, listProperty));
        populateListNodes(listNode);
        return listNode;
    }

    public void populateListNodes(Node parentNode) {
        ClassDatabase cdb = parentNode.getClassDatabase();
        ListNodeContext listNodeContext = parentNode.getListNodeContext();
        Collection list = listNodeContext.getCollection();
        for (Object o : list) {
            ObjectLocation objectLocation = parentNode.toObjLocation();
            createNode(parentNode, cdb.findOrCreateObjMeta(objectLocation, o));
        }
    }

    public Node refresh(Node node) {
        Node newNode = null;
        if (node.isRoot()) {
            newNode = createTree();
        } else {
            Node parent = node.getParent();
            int oldIndex = parent.indexOf(node);
            appContainer.removeNode(node);
            if (node.isObjectNode()) {
                newNode = createNode(parent, node.objectMeta(), oldIndex);
            } else {
                newNode = createListNode(parent, node.getListNodeContext().getContainerProperty(), oldIndex);
            }
        }
        appContainer.getMainPanel().repaint();
        return newNode;
    }
}
