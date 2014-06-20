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

import net.sf.xapp.application.api.Command;
import net.sf.xapp.application.api.Node;
import net.sf.xapp.application.commands.*;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.objectmodelling.api.Rights;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ContainerProperty;
import net.sf.xapp.objectmodelling.core.ObjectLocation;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class ListNodeContext {
    private final ObjectLocation objectLocation;

    public ListNodeContext(ObjectLocation objectLocation) {
        this.objectLocation = objectLocation;
    }

    public ObjectLocation getObjectLocation() {
        return objectLocation;
    }

    public Collection getCollection() {
        return objectLocation.getCollection();
    }

    public List getList() {
        return (List) getCollection();
    }

    public ContainerProperty getContainerProperty() {
        return (ContainerProperty) objectLocation.getProperty();
    }

    public ObjectMeta getListOwner() {
        return objectLocation.getObj();
    }

    public List<ClassModel> getValidImplementations() {
        return getContainerProperty().getContainedTypeClassModel().getValidImplementations();
    }

    public List<Command> createCommands(Node node, CommandContext commandContext) {
        List<Command> commands = new ArrayList<Command>();
        if (commandContext == CommandContext.SEARCH) return commands;
        ContainerProperty prop = getContainerProperty();
        ClassModel classModel = prop.getContainedTypeClassModel();
        if (prop.containsReferences() && prop.isAllowed(Rights.SELECT_OBJECTS)) {
            commands.add(new GetReferencesCommand());
        } else if (!prop.containsReferences()) {
            if (prop.isAllowed(Rights.CREATE) && classModel.isAbstract()) {
                List<ClassModel> validImplementations = getValidImplementations();
                List<CreateCommand> createCommands = new ArrayList<CreateCommand>();
                if (!Modifier.isAbstract(classModel.getContainedClass().getModifiers())) {
                    createCommands.add(new CreateCommand(classModel));
                }
                for (ClassModel validImpl : validImplementations) {
                    createCommands.add(new CreateCommand(validImpl));
                }
                if (commandContext == CommandContext.POP_UP && createCommands.size() <= SwingUtils.MAX_CREATE_COMMANDS_IN_POP_UP) {
                    commands.addAll(createCommands);
                } else {
                    commands.add(new PopUpCreateCommand(createCommands, null, null));
                }
            } else {
                if (prop.isAllowed(Rights.CREATE)) {
                    commands.add(new CreateCommand(classModel));
                }
            }
            if (prop.isAllowed(Rights.PASTE)) {
                commands.add(new PasteXMLCommand());
            }
        }
        //maybe add a paste option
        Clipboard clipboard = node.getAppContainer().getClipboard();
        List<Object> clipboardObjects = clipboard.getClipboardObjects();
        if (!clipboardObjects.isEmpty() &&
                clipboard.areAllInstanceOf(classModel.getContainedClass()) &&
                !clipboard.listContainsAny(getCollection())) {
            commands.add(new PasteCommand(node));
        }

        return commands;
    }
}
