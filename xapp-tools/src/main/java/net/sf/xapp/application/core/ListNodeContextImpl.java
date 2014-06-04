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

import net.sf.xapp.application.api.*;
import net.sf.xapp.application.commands.*;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.objectmodelling.api.Rights;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ContainerProperty;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 */
public class ListNodeContextImpl implements ListNodeContext
{
    private ContainerProperty m_listProperty;
    private ObjectMeta m_listOwner;
    private Node node;

    public ListNodeContextImpl(ContainerProperty listProperty, ObjectMeta listOwner)
    {
        m_listProperty = listProperty;
        m_listOwner = listOwner;
    }

    public Collection getCollection()
    {
        return m_listProperty.getCollection(m_listOwner.getInstance());
    }

    public List getList()
    {
        return (List) getCollection();
    }

    public ContainerProperty getContainerProperty()
    {
        return m_listProperty;
    }

    public ObjectMeta getListOwner()
    {
        return m_listOwner;
    }

    public List<ClassModel> getValidImplementations()
    {
        return m_listProperty.getContainedTypeClassModel().getValidImplementations();
    }

    @Override
    public boolean contains(ObjectMeta instance) {
        return m_listProperty.contains(m_listOwner.getInstance(), instance);
    }

    @Override
    public void add(ObjectMeta instance) {
        m_listProperty.add(m_listOwner.getInstance(), instance);
    }

    public List<Command> createCommands(Node node, CommandContext commandContext)
    {
        List<Command> commands = new ArrayList<Command>();
        if(commandContext == CommandContext.SEARCH) return commands;
        ClassModel classModel = m_listProperty.getContainedTypeClassModel();
        if (m_listProperty.containsReferences() && m_listProperty.isAllowed(Rights.SELECT_OBJECTS))
        {
            commands.add(new GetReferencesCommand());
        }
        else if(!m_listProperty.containsReferences())
        {
            if (m_listProperty.isAllowed(Rights.CREATE) && classModel.isAbstract())
            {
                List<ClassModel> validImplementations = getValidImplementations();
                List<CreateCommand> createCommands = new ArrayList<CreateCommand>();
                if(!Modifier.isAbstract(classModel.getContainedClass().getModifiers())) {
                    createCommands.add(new CreateCommand(classModel));
                }
                for (ClassModel validImpl : validImplementations)
                {
                    createCommands.add(new CreateCommand(validImpl));
                }
                if (commandContext==CommandContext.POP_UP && createCommands.size()<= SwingUtils.MAX_CREATE_COMMANDS_IN_POP_UP)
                {
                    commands.addAll(createCommands);
                }
                else
                {
                    commands.add(new PopUpCreateCommand(createCommands, null,null));
                }
            }
            else
            {
                if (m_listProperty.isAllowed(Rights.CREATE))
                {
                    commands.add(new CreateCommand(classModel));
                }
            }
            if (m_listProperty.isAllowed(Rights.PASTE))
            {
                commands.add(new PasteXMLCommand());
            }
        }
        //maybe add a paste option
        Clipboard clipboard = node.getAppContainer().getClipboard();
        List<Object> clipboardObjects= clipboard.getClipboardObjects();
        if (!clipboardObjects.isEmpty() &&
                clipboard.areAllInstanceOf(classModel.getContainedClass()) &&
                !clipboard.listContainsAny(getCollection()))
        {
            commands.add(new PasteCommand(node));
        }

        return commands;
    }

    public void setNode(Node node)
    {
        this.node = node;
    }
}
