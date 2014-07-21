package net.sf.xapp.codegen.model;


import net.sf.xapp.marshalling.api.StringSerializable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 */
public class PropertyMap extends LinkedHashMap<String, String> implements StringSerializable {
    public PropertyMap(String properties) {
        if (properties!=null) {
            readString(properties);
        }
    }

    @Override
    public void readString(String str) {
        String[] lines = str.split("\n");
        for (String line : lines) {
            String[] args = line.split("=");
            put(args[0],args.length==1 ? "true" : args[1]);
        }
    }

    @Override
    public String writeString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : this.entrySet()) {
            String value = entry.getValue();
            sb.append(entry.getKey());
            if(!value.equals("true")) {
                sb.append("=").append(value);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
