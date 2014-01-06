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

import net.sf.xapp.application.api.ApplicationContainer;
import net.sf.xapp.application.api.GUIContext;
import net.sf.xapp.marshalling.Marshaller;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.utils.XappException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DefaultGUIContext implements GUIContext
{
    private File m_currentFile;
    private Object m_instance;
    private ClassModel m_rootType;
    private Marshaller m_marshaller;
    private Unmarshaller m_unmarshaller;
    private ClassDatabase m_classDatabase;
    private String m_encoding;
    private ApplicationContainer m_applicationContainer;
    private SimpleDateFormat m_dateFormat;

    public DefaultGUIContext(File currentFile, ClassDatabase classDatabase, Object instance)
    {
        this(currentFile, classDatabase, instance, "UTF-8");
    }

    public DefaultGUIContext(File currentFile, ClassDatabase classDatabase, Object instance, String encoding)
    {
        m_currentFile = currentFile;
        m_instance = instance;
        m_classDatabase = classDatabase;
        Class containedClass = instance.getClass();
        m_rootType = classDatabase.getClassModel(containedClass);
        m_marshaller =  new Marshaller(containedClass, m_classDatabase, true);
        m_unmarshaller = new Unmarshaller(classDatabase.getClassModel(containedClass));
        m_encoding = encoding;
        m_dateFormat = new SimpleDateFormat("HH:mm:ss");
    }

    public File getCurrentFile()
    {
        return m_currentFile;
    }

    public Object getInstance()
    {
        return m_instance;
    }

    public ClassModel getRootType()
    {
        return m_rootType;
    }

    public void newObjectInstance()
    {
        m_instance = m_rootType.newInstance(null);
        m_currentFile = null;
    }

    public void saveToFile(File file)
    {
        m_currentFile = file;
        try
        {              //TODO marshaller now default file marshalling to UTF-8 so this code could be shortened
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, m_encoding );
            m_marshaller.marshal(osw, m_instance);
            osw.flush();
            osw.close();
            m_applicationContainer.setStatusMessage("saved to file: "+file + " "+m_dateFormat.format(new Date(System.currentTimeMillis())));
        }
        catch (Exception e)
        {
            throw new XappException(e);
        }
    }

    public void saveToFile()
    {
        saveToFile(m_currentFile);
    }

    public Object openFile(File selectedFile)
    {
        //reset the class model
        m_classDatabase.reset();
        m_unmarshaller.reset(m_classDatabase);
        m_marshaller.reset(m_classDatabase);
        m_currentFile = selectedFile;
        try
        {
            m_instance = m_unmarshaller.unmarshal(selectedFile.getAbsolutePath());
        }
        catch (Exception e)
        {
            throw new XappException(e);
        }
        return m_instance;
    }

    public ClassDatabase getClassDatabase()
    {
        return m_classDatabase;
    }

    public void init(ApplicationContainer applicationContainer)
    {
        m_applicationContainer = applicationContainer;
    }
}
