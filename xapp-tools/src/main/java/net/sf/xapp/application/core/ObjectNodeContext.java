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

import static net.sf.xapp.objectmodelling.api.Rights.*;

import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ObjectMeta;
import net.sf.xapp.objectmodelling.core.Property;

import java.util.ArrayList;
import java.util.List;

public class ObjectNodeContext {
    private Node node;
    private ObjectMeta objectMeta;

    public ObjectNodeContext(NodeImpl node, ObjectMeta objMeta) {
        this.node = node;
        objectMeta = objMeta;
    }

    public boolean hasToStringMethod() {
        return classModel().hasMethod("toString");
    }

    private ClassModel classModel() {
        return objectMeta().getClassModel();
    }

    public Object instance() {
        return objectMeta().getInstance();
    }

    public ObjectMeta objectMeta() {
        return objectMeta;
    }

    @Override
    public String toString() {
        if(hasToStringMethod()) {
            String strValue = instance().toString();
            if (strValue != null && strValue.length() > 50)
            {
                strValue = strValue.substring(0, 50);
            }
            return strValue;
        } else {
            Property property = objectMeta.getHome().getProperty();
            return instance().getClass().getSimpleName() + (property != null ? ":" + property.getName() : "");
        }
    }

    public List<Command> createCommands(CommandContext commandContext) {
        List<Command> commands = new ArrayList<Command>();
        if (canEdit()) commands.add(new EditCommand());
        //add commands that are only allowed for objects in a list:
        if (commandContext != CommandContext.SEARCH) {
            if (classModel().isAllowed(DELETE) || node.isReference()) {
                commands.add(new RemoveCommand());
            }
            if (classModel().isAllowed(MOVE_UP_OR_DOWN)) {
                commands.add(new MoveUpCommand());
                commands.add(new MoveDownCommand());
            }
            //add commands that are only allowed when the surrounding list does NOT contain references
            Node parentNode = node.getParent();
            if (!parentNode.containsReferences()) {
                //COPY and COPY_XML
                if (classModel().isAllowed(CUT_COPY) && objectMeta.isCloneable()) {
                    commands.add(new CopyCommand());
                    commands.add(new CopyXMLCommand());
                }
                //CUT
                if (classModel().isAllowed(CUT_COPY)) {
                    commands.add(new CutCommand());
                }
                //CHANGE_TYPE
                List<ClassModel> validImpls = parentNode.getListNodeContext().getValidImplementations();
                if (validImpls.size() > 1 && classModel().isAllowed(CHANGE_TYPE)) {
                    commands.add(new ChangeTypeCommand());
                }
            }
        }
        return commands;
    }

    public boolean canEdit() {
        return classModel().isAllowed(EDIT);
    }
}
