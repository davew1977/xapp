package net.sf.xapp.application.editor.widgets;

import net.sf.xapp.application.editor.text.TextEditor;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.marshalling.stringserializers.StringMapSerializer;

import java.awt.*;
import java.util.Arrays;
import java.util.regex.Matcher;

/**
* Created by oldDave on 05/02/2015.
*/
public class PropsEditor extends TextEditor {
    private Color nameColor = new Color(5,32,144);
    private Color valueColor = new Color(0, 128, 0);

    @Override
    public void handleNewText(int i, String s, Line l, java.util.List<Line> lines) {
        for (Line line : lines) {
            Matcher matcher = StringMapSerializer.PROP_PATTERN.matcher(line.m_text);
            if(matcher.find()) {
                setChars(line.m_startIndex + matcher.start(1), matcher.group(1).length(), true, false, nameColor);
                setChars(line.m_startIndex + matcher.start(2), matcher.group(2).length(), true, false, valueColor);
            }
        }

    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public void handleTextRemoved(int i, int i1, Line line, String s) {
        handleNewText(i, null, null, Arrays.asList(line));
    }

    public static void main(String[] args) {
        PropsEditor p = new PropsEditor();
        SwingUtils.showInFrame(p);
    }
}
