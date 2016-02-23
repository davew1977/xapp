package net.sf.xapp.application.editor.widgets;

import java.util.List;

/**
 */
public interface PropsProvider {
    List<String> getKeys();
    List<String> getValues(String key);
}
