package net.sf.xapp.objserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

/**
 * © 2013 Newera Education Ltd
 * Created by dwebber
 */
public class SimpleObjLoader implements ObjLoader {
    private Class type;
    private List<String> xmlClassPaths;

    public SimpleObjLoader(Class type, List<String> xmlClassPaths) {
        this.type = type;
        this.xmlClassPaths = xmlClassPaths;
    }

    @Override
    public List<ObjectMeta> loadAll() {
        List<ObjectMeta> result = new ArrayList<ObjectMeta>();
        for (String xmlClassPath : xmlClassPaths) {

            Unmarshaller unmarshaller = new Unmarshaller(type);
            unmarshaller.getClassDatabase().setMaster();
            unmarshaller.getClassDatabase().setRevision(0L);
            result.add(unmarshaller.unmarshal(getClass().getResourceAsStream(xmlClassPath)));
        }

        return result;
    }
}
