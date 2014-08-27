/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import net.sf.xapp.annotations.application.Container;
import net.sf.xapp.annotations.objectmodelling.PreInit;
import net.sf.xapp.annotations.objectmodelling.Transient;
import net.sf.xapp.application.api.Launcher;
import net.sf.xapp.marshalling.Marshaller;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.api.ClassDatabase;
import net.sf.xapp.objectmodelling.api.InspectionType;
import net.sf.xapp.objectmodelling.core.ClassModel;
import net.sf.xapp.objectmodelling.core.ClassModelManager;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.util.List;

/**
 * "bean" storing miscelaneous test data
 */
@Container(listProperty = "scripts")
public class TestData
{
    public List<Script> scripts;
    @Transient
    public ClassModel<TestData> m_cm;

    @PreInit
    public void init(ObjectMeta objectMeta)
    {
        m_cm = objectMeta.getClassModel();

    }

    private <T> T getInstance(Class<T> cl, String id)
    {
        ClassDatabase<?> cdb = m_cm.getClassDatabase();
        return cdb.getInstanceNoCheck(cl, id);
    }

    public static void main(String[] args)
    {
        Launcher.run(TestData.class, new ViewerApplication(), args[0], InspectionType.FIELD);
    }

    public static TestData load()
    {
        return load("/test-data.xml");
    }

    public static TestData load(String url)
    {
        return Unmarshaller.load(TestData.class, "classpath://" + url, InspectionType.FIELD);
    }

    public void save(String toFile)
    {
        new Marshaller<TestData>(TestData.class, new ClassModelManager<TestData>(TestData.class, InspectionType.FIELD), true).marshal(toFile, this);
    }

    public SpringConfig getSpringConfig(String id)
    {
        SpringConfig config = getInstance(SpringConfig.class, id);
        if(config==null)
        {
            throw new RuntimeException(String.format("SpringConfig with key %s does not exist", id));
        }
        return config;
    }

    public Script getScript(String id)
    {
        return getInstance(Script.class, id);
    }

    @Override
    public String toString()
    {
        return "Misc Admin Functions";
    }
}