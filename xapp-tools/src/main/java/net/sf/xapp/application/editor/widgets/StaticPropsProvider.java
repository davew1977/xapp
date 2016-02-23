package net.sf.xapp.application.editor.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */
public class StaticPropsProvider extends ArrayList<String> implements PropsProvider {
    public StaticPropsProvider(Collection<? extends String> c) {
        super(c);
    }

    @Override
    public List<String> getKeys() {
        return this;
    }

    @Override
    public List<String> getValues(String key) {
        return new ArrayList<>();
    }
}
