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
package net.sf.xapp.application.editor;

import net.sf.xapp.application.api.ObjectWidget;
import net.sf.xapp.objectmodelling.api.InspectionType;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ClassModelManager;
import net.sf.xapp.utils.XappException;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditorManager
{
    private static EditorManager ourInstance = new EditorManager();

    public static EditorManager getInstance()
    {
        return ourInstance;
    }

    public static final int MAX_INSTANCES = 1;
    private HashMap<ClassModel, List<Editor>> m_guiMap;

    private EditorManager()
    {
        m_guiMap = new HashMap<ClassModel, List<Editor>>();
    }

    public Editor getEditor(EditableContext editableContext, EditorListener listener)
    {
        ClassModel classModel = editableContext.getClassModel();
        Class aClass = classModel.getContainedClass();
        List<Editor> editors = m_guiMap.get(classModel);
        if (editors == null)
        {
            editors = new ArrayList<Editor>();
            m_guiMap.put(classModel, editors);
        }
        //find an available defaultGui
        for (Editor editor : editors)
        {
            if (!editor.getMainWindow().isVisible())
            {
                editor.setEditableContext(editableContext);
                editor.setGuiListener(listener);
                return editor;
            }
        }
        //can we create a gui?
        if(editors.size()==MAX_INSTANCES)
        {
            throw new XappException("no available defaultGui instance: class: "+aClass);
        }
        ObjectWidget objectWidget = EditorUtils.createBoundObject(classModel, editableContext.getTarget());
        Editor editor;
        if(objectWidget !=null)
        {
            editor = new SpecialEditor(classModel, objectWidget);
        }
        else
        {
            editor = new DefaultEditor();
        }
        editor.setEditableContext(editableContext);
        editor.setGuiListener(listener);
        editors.add(editor);
        return editor;
    }

    public void reset()
    {
        m_guiMap.clear();
    }

    /**
     * Overloading where a new classmodel is created from scratch
     * @param p
     * @return
     */
    public boolean edit(Frame comp, Object p)
    {
        return edit(comp, new ClassModelManager(p.getClass(), InspectionType.FIELD).getRootClassModel(), p);
    }


    public void edit(Frame comp, Object p, EditorListener editorListener)
    {
        Editor editor = getEditor(new SingleTargetEditableContext(
                new ClassModelManager(p.getClass(), InspectionType.FIELD).getRootClassModel(), p,
                SingleTargetEditableContext.Mode.EDIT), editorListener);
        editor.getMainFrame().setVisible(true);
    }

    /**
     * Throw up an editor dialog for the given object and block while it's edited
     * @param comp
     * @param classModel
     * @param p
     * @return true if the object was edited, false if the user cancelled
     */
    public boolean edit(Frame comp, ClassModel classModel, Object p)
    {
        Editor editor = getEditor(new SingleTargetEditableContext(classModel, p, SingleTargetEditableContext.Mode.EDIT), new EditorListener.NullEditorListener());
        editor.getMainDialog(comp, true).setVisible(true);
        return !editor.wasCancelled();
    }
}
