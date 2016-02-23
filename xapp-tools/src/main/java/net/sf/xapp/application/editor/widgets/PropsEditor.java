package net.sf.xapp.application.editor.widgets;

import net.sf.xapp.application.editor.text.TextEditor;
import net.sf.xapp.application.editor.text.Word;
import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.marshalling.stringserializers.StringMapSerializer;
import net.sf.xapp.utils.CollectionsUtils;
import net.sf.xapp.utils.Filter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

/**
* Created by oldDave on 05/02/2015.
*/
public class PropsEditor extends TextEditor {
    private Color nameColor = new Color(5,32,144);
    private Color valueColor = new Color(0, 128, 0);
    private PropsProvider propsProvider;

    public PropsEditor() {
        addAction("control SPACE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Line currentLine = getCurrentLine();
                int eqIndex = currentLine.m_text.indexOf('=');
                final Word word = currentLine.wordAtCaret();
                List<String> fullSet = word.start < eqIndex ? propsProvider.getKeys() : propsProvider.getValues(currentLine.m_text.substring(0, eqIndex));
                final String stem = word.wordToCaret();
                List<String> suggestions = CollectionsUtils.filter(fullSet, new Filter<String>() {
                    @Override
                    public boolean matches(String s) {
                        return s.startsWith(stem);
                    }
                });
                if (!suggestions.isEmpty()) {
                    JPopupMenu popUp = newPopUp();
                    for (final String suggestion : suggestions) {
                        popUp.add(new JMenuItem(new AbstractAction(suggestion) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                replaceWordAtCaret(word, suggestion);
                            }
                        }));
                    }
                    showPopUp();
                }
            }
        });
    }

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

    public void setKeysEnum(List<String> keysEnum) {
        this.propsProvider = new StaticPropsProvider(keysEnum);
    }

    public void setPropsProvider(PropsProvider propsProvider) {
        this.propsProvider = propsProvider;
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
        p.setKeysEnum(Arrays.asList("choose","between","this","and","joober","bush","brown","clegg","arnold"));
        p.setPreferredSize(new Dimension(400,400));
        SwingUtils.showInFrame(p);
    }
}
