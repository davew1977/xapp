package net.sf.xapp.application.editor.text;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class Word {
    public final String value;
    public final int start;
    public final int end;
    public final int cutIndex;


    public Word(String value, int start, int end, int cutIndex) {
        this.value = value;
        this.start = start;
        this.end = end;
        this.cutIndex = cutIndex;
        assert cutIndex >=0 && cutIndex<=length();
    }

    public int length() {
        return value.length();
    }

    public String wordToCaret() {
        return value.substring(0, cutIndex);
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }
}
