package net.sf.xapp.marshalling.stringserializers;

import net.sf.xapp.marshalling.api.StringSerializer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oldDave on 05/03/2015.
 */
public class StringMapSerializer implements StringSerializer<Map<String,String>> {
    public static final Pattern PROP_PATTERN = Pattern.compile("(.*)=(.*)");

    @Override
    public Map<String, String> read(String str) {
        return _read(str);
    }

    @Override
    public String write(Map<String, String> obj) {
        return _write(obj);
    }

    public static String _write(Map<String, String> obj) {
        if(obj == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : obj.entrySet()) {
            sb.append(e.getKey()).append("=").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }

    public static Map<String, String> _read(String str) {
        if(str == null) {
            return null;
        }
        Map<String, String> result = new LinkedHashMap<>();
        String[] lines = str.split("\n");
        for (String line : lines) {
            Matcher matcher = PROP_PATTERN.matcher(line);
            if(matcher.find()) {
                result.put(matcher.group(1), matcher.group(2));
            }
        }

        return result;
    }

    @Override
    public String validate(String text) {
        return null;
    }
}
