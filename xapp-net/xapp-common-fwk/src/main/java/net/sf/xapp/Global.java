package net.sf.xapp;

import net.sf.xapp.marshalling.Unmarshaller;
import ngpoker.codegen.model.ComplexType;
import ngpoker.codegen.model.Message;
import ngpoker.codegen.model.Model;
import net.sf.xapp.net.common.framework.ObjectType;
import net.sf.xapp.net.common.framework.TransportObject;
import ngpoker.common.types.MessageTypeEnum;
import ngpoker.common.types.ObjectTypeEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class must be replaced (as opposed to overridden) by the most specific project using the framework
 */
public class Global {
    public static Model model = Unmarshaller.load(Model.class, "classpath:///domain-model.xml");
    public static final Map<Integer, ObjectType> ID_MAP = new HashMap<Integer, ObjectType>();
    public static final Map<String, ObjectType> NAME_MAP = new HashMap<String, ObjectType>();

    static {
        ID_MAP.putAll(MessageTypeEnum.createTypeMap());
        ID_MAP.putAll(ObjectTypeEnum.createTypeMap());

        for (MessageTypeEnum messageTypeEnum : MessageTypeEnum.values()) {
            NAME_MAP.put(messageTypeEnum.name(), messageTypeEnum);
        }
        for (ObjectTypeEnum objectTypeEnum : ObjectTypeEnum.values()) {
            NAME_MAP.put(objectTypeEnum.name(), objectTypeEnum);
        }
    }

    public static TransportObject create(int id) {
        return getObjectType(id).create();
    }

    public static TransportObject create(String name) {
        return getObjectType(name).create();
    }

    public static ObjectType getObjectType(int id) {
        return ID_MAP.get(id);
    }

    public static ObjectType getObjectType(String name) {
        return NAME_MAP.get(name);
    }

    public static Map<String, ComplexType> deriveAllTypes() {
        return model.deriveAllTypes();
    }

    public static List<Message> allMessages() {
        return model.deriveAllMessages();
    }
}
