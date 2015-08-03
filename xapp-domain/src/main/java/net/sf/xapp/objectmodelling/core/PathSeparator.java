package net.sf.xapp.objectmodelling.core;

/**
 * Created by oldDave on 01/08/2015.
 */
public enum PathSeparator {
    FORWARD_SLASH("/", "/"),
    DOT(".", "\\.");
    private final String regexp;
    private final String sep;

    PathSeparator(String sep, String regexp) {
        this.sep = sep;
        this.regexp = regexp;
    }

    @Override
    public String toString() {
        return sep;
    }

    public String regExp() {
        return regexp;
    }
}
