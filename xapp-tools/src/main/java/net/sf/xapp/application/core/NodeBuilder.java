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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates the JTree nodes according to the model
 */
public class NodeBuilder {
    private static Logger log = LoggerFactory.getLogger(NodeBuilder.class);
    private ApplicationContainerImpl appContainer;
    private ClassDatabase cdb;
    private Map<Object, Node> nodeMap;
    private Map<Object, List<Node>> refNodeMap;


    public NodeBuilder(ApplicationContainerImpl appContainer) {
        this.appContainer = appContainer;
        cdb = this.appContainer.getClassDatabase();
        nodeMap = new HashMap<>();
        refNodeMap = new HashMap<>();
    }

    public Node createTree() {
        ObjectMeta objectMeta = appContainer.getGuiContext().getObjectMeta();
        Node node = createNode(null, objectMeta, null, 0);
        appContainer.getMainTree().setModel(new DefaultTreeModel(node.getJtreeNode()));
        return node;
    }

    public Node createNode(Node parentNode, ObjectMeta objMeta, Property locProperty) {
        return createNode(parentNode, objMeta, locProperty, parentNode.numChildren());
    }

    public Node createNode(Node parentNode, ObjectMeta objMeta, Property locProperty, int insertIndex) {
        if (!appContainer.getNodeFilter().accept(objMeta)) {
            return null;
        }
        //parent node may be:
        // 1) a list node containing objects
        // 2) a list node containing references
        // 3) an object node containing a set of objects (if it is a "container" class)
        // 4) an object node containing one or more complex objects (ones important enough to have their own nodes)
        Node newNode = new NodeImpl(appContainer, parentNode, insertIndex, objMeta, locProperty);
        registerNode(newNode);
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
                if (listProperty.isStringSerializable()) continue; //skip string lists
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
                    createNode(newNode, propValueObjMeta, property);
                }
            }
        }

        return newNode;
    }

    private void registerNode(Node newNode) {
        Object objId = newNode.objectMeta().objId();
        if(newNode.isReference()) {
            List<Node> nodes = refNodeMap.get(objId);
            if(nodes==null) {
                nodes = new ArrayList<>();
                refNodeMap.put(objId, nodes);
            }
            nodes.add(newNode);
        } else {
            nodeMap.put(objId, newNode);
        }
    }

    public List<Node> getRefNodes(Object objId) {
        List<Node> nodes = refNodeMap.get(objId);
        return nodes != null ? nodes : new ArrayList<Node>();
    }

    public Node getNode(Object objId) {
        return nodeMap.get(objId);
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
            ObjectMeta objMeta = cdb.find(o);
            if (objMeta != null) {
                createNode(parentNode, objMeta, null);
            } else {
                log.debug("object not registered with CDB: " + o);
            }
            //createNode(parentNode, cdb.findOrCreateObjMeta(parentNode.toObjLocation(), o));
        }
    }

    public Node refresh(Node node) {
        Node newNode = null;
        if (node.isRoot()) {
            newNode = createTree();
        } else {
            Node parent = node.getParent();
            int oldIndex = parent.indexOf(node);
            appContainer.removeNode(node, false, false);
            if (node.isObjectNode()) {
                newNode = createNode(parent, node.objectMeta(), node.getLocProperty(), oldIndex);
            } else {
                newNode = createListNode(parent, node.getListNodeContext().getContainerProperty(), oldIndex);
            }
        }
        appContainer.getMainPanel().repaint();
        return newNode;
    }

    /**
     * find the node depending on the location, could be the main node or a reference node
     */
    public Node getNode(Long id, ObjectLocation objectLocation) {
        Node node = getNode(id);
        if(node.myObjLocation().equals(objectLocation)) {
            return node;
        }
        List<Node> refNodes = getRefNodes(id);
        for (Node refNode : refNodes) {
            if(refNode.myObjLocation().equals(objectLocation)) {
                return refNode;
            }
        }
        return null;
    }

    public void nodeRemoved(Node node) {
        Object id = node.objectMeta().objId();
        if(node.isReference()) {
            List<Node> refNodes = getRefNodes(id);
            refNodes.remove(node);
        } else {
            nodeMap.remove(id);
        }
    }
}
