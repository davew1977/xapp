package net.sf.xapp.marshalling.stringserializers;

import net.sf.xapp.marshalling.api.StringSerializer;
import net.sf.xapp.utils.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oldDave on 05/03/2015.
 */
public class StringMapSerializer implements StringSerializer<Map<?,?>> {
    public static final Pattern PROP_PATTERN = Pattern.compile("(.*)=(.*)");
    private Class mapKeyType = String.class;

    @Override
    public Map<?, ?> read(String str) {
        return _read(mapKeyType, String.class, str);
    }

    @Override
    public String write(Map<?, ?> obj) {
        return _write(obj);
    }

    public static String _write(Map<?, ?> obj) {
        if(obj == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> e : obj.entrySet()) {
            Object value = e.getValue();
            if(e.getValue() instanceof Collection) {
                Collection col = (Collection) e.getValue();
                value = StringUtils.join(col, ",");
            }
            sb.append(e.getKey()).append("=").append(value).append("\n");
        }
        return sb.toString();
    }

    public static Map<?, ?> _read(Class<?> mapKeyType, Type type, String str) {
        if(str == null) {
            return null;
        }
        Map<Object, Object> result = new LinkedHashMap<>();
        String[] lines = str.split("\n");
        for (String line : lines) {
            Matcher matcher = PROP_PATTERN.matcher(line);

            if(matcher.find()) {
                String keyString = matcher.group(1);
                Object key;
                if(mapKeyType.equals(String.class)) {
                    key = keyString;
                } else {
                    assert mapKeyType.isEnum();
                    key = EnumListSerializer.readSingleValue(keyString, mapKeyType);
                }
                String value = matcher.group(2);
                if(value.equals("")) {
                   value = null;
                }
                result.put(key, convert(type, value));
            }
        }

        return result;
    }

    public static Object convert(Type propertyType, String value) {
        if(propertyType instanceof Class) {
            return convert((Class) propertyType, value);
        } else {
            ParameterizedType parameterizedType = (ParameterizedType) propertyType;
            Class rawType = (Class) parameterizedType.getRawType();
            if(Collection.class.isAssignableFrom(rawType)) {
                String[] args = value.split(",");
                Collection col;
                if(Set.class.isAssignableFrom(rawType)) {
                    col = new HashSet();
                } else {
                    col = new ArrayList();
                }
                for (String arg : args) {
                    col.add(convert(parameterizedType.getActualTypeArguments()[0], arg));
                }
                return col;
            }
        }
        throw new UnsupportedOperationException();
    }


    public static Object convert(Class propertyClass, String value) {
        if (propertyClass.equals(String.class)) {
            return value;
        } else if (propertyClass.equals((boolean.class)) || propertyClass.equals(Boolean.class)) {
            return Boolean.valueOf(value);
        } else if (propertyClass.equals((int.class)) || propertyClass.equals(Integer.class)) {
            return new Integer(value);
        } else if (propertyClass.equals((short.class)) || propertyClass.equals(Short.class)) {
            return new Short(value);
        } else if (propertyClass.equals((byte.class)) || propertyClass.equals(Byte.class)) {
            return new Byte(value);
        } else if (propertyClass.equals((char.class)) || propertyClass.equals(Character.class)) {
            return value.charAt(0);
        } else if (propertyClass.equals((float.class)) || propertyClass.equals(Float.class)) {
            return new Float(value);
        } else if (propertyClass.equals((double.class)) || propertyClass.equals(Double.class)) {
            return new Double(value);
        } else if (propertyClass.equals((long.class)) || propertyClass.equals(Long.class)) {
            return new Long(value);
        } else if (propertyClass.isEnum()) {
            return Enum.valueOf(propertyClass, value);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String validate(String text) {
        return null;
    }
}
