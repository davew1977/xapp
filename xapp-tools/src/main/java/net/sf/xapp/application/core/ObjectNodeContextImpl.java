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
import net.sf.xapp.application.api.ObjectNodeContext;
import static net.sf.xapp.application.api.ObjectNodeContext.ObjectContext.IN_LIST;
import net.sf.xapp.application.commands.*;
import static net.sf.xapp.objectmodelling.api.Rights.*;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.Property;

import java.util.ArrayList;
import java.util.List;

public class ObjectNodeContextImpl implements ObjectNodeContext
{
    private ClassModel m_classModel;
    private Object m_instance;
    private ObjectContext m_objectContext;
    private Property property;

    public ObjectNodeContextImpl(Property property, ClassModel classModel, Object instance, ObjectContext objectContext)
    {
        this.property = property;
        m_classModel = classModel;
        m_instance = instance;
        m_objectContext = objectContext;
    }

    @Override
    public boolean hasToStringMethod() {
        return getClassModel().hasMethod("toString");
    }

    @Override
    public Property getProperty() {
        return property;
    }

    public ClassModel getClassModel()
    {
        return m_classModel;
    }

    public Object getInstance()
    {
        return m_instance;
    }

    public ObjectContext getObjectContext()
    {
        return m_objectContext;
    }

    public List<Command> createCommands(Node node, CommandContext commandContext)
    {
        List<Command> commands = new ArrayList<Command>();      
        if (canEdit()) commands.add(new EditCommand());
        //add commands that are only allowed for objects in a list:
        if (m_objectContext == IN_LIST && commandContext!= CommandContext.SEARCH)
        {
            if (m_classModel.isAllowed(DELETE) || node.isReference())
            {
                commands.add(new RemoveCommand());
            }
            if(m_classModel.isAllowed(MOVE_UP_OR_DOWN))
            {
                commands.add(new MoveUpCommand());
                commands.add(new MoveDownCommand());
            }
            //add commands that are only allowed when the surrounding list does NOT contain references
            Node parentNode = node.getParent();
            if (!parentNode.containsReferences())
            {
                //COPY and COPY_XML
                if (m_classModel.isAllowed(CUT_COPY) && m_instance instanceof Cloneable)
                {
                    commands.add(new CopyCommand());
                    commands.add(new CopyXMLCommand());
                }
                //CUT
                if (m_classModel.isAllowed(CUT_COPY)) commands.add(new CutCommand());
                //CHANGE_TYPE
                List<ClassModel> validImpls = parentNode.getListNodeContext().getValidImplementations();
                if (validImpls.size() > 1 && m_classModel.isAllowed(CHANGE_TYPE))
                {
                    commands.add(new ChangeTypeCommand());
                }
            }
        }
        return commands;
    }

    public boolean canEdit()
    {
        return m_classModel.isAllowed(EDIT);
    }
}
