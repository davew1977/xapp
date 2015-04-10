package net.sf.xapp.marshalling.stringserializers;

import net.sf.xapp.marshalling.api.StringSerializer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oldDave on 05/03/2015.
 */
public class StringMapSerializer implements StringSerializer<Map<?,String>> {
    public static final Pattern PROP_PATTERN = Pattern.compile("(.*)=(.*)");
    private Class mapKeyType = String.class;

    @Override
    public Map<?, String> read(String str) {
        return _read(mapKeyType, str);
    }

    @Override
    public String write(Map<?, String> obj) {
        return _write(obj);
    }

    public static String _write(Map<?, String> obj) {
        if(obj == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, String> e : obj.entrySet()) {
            sb.append(e.getKey()).append("=").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }

    public static Map<?, String> _read(Class<?> mapKeyType, String str) {
        if(str == null) {
            return null;
        }
        Map<Object, String> result = new LinkedHashMap<>();
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
                result.put(key, matcher.group(2));
            }
        }

        return result;
    }

    @Override
    public String validate(String text) {
        return null;
    }
}
