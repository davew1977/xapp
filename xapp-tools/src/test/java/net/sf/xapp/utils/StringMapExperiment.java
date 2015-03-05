package net.sf.xapp.utils;

import net.sf.xapp.marshalling.Marshaller;
import net.sf.xapp.marshalling.Unmarshaller;
import net.sf.xapp.objectmodelling.api.InspectionType;
import net.sf.xapp.objectmodelling.core.ClassModelManager;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by oldDave on 05/03/2015.
 */
public class StringMapExperiment {

    public static void main(String[] args) {
        Experiment e = new Experiment();
        e.props = new LinkedHashMap<>();
        e.props.put("hello", "there");
        e.props.put("plop", "therethis is me");

        Experiment2 e2 = new Experiment2();
        e2.props.add("hi");

        Object o = e;

        String s = Marshaller.toXML(o, InspectionType.FIELD);
        System.out.println(s);
        ObjectMeta objectMeta = new ClassModelManager(o.getClass(), InspectionType.FIELD).getRootUnmarshaller().unmarshalString(s);
        s = Marshaller.toXML(objectMeta.getInstance(), InspectionType.FIELD);
        System.out.println(s);

    }

    public static class Experiment {
        Map<String, String> props;
    }

    public static class Experiment2 {
        List<String> props = new ArrayList<>();
    }
}
