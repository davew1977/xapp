package net.sf.xapp.examples.school.model;

import net.sf.xapp.annotations.application.EditorWidget;
import net.sf.xapp.annotations.marshalling.FormattedText;
import net.sf.xapp.application.editor.widgets.FreeTextPropertyWidget;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 5/9/14
 * Time: 7:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class TextFile extends FileMeta {
    private String text;
    public TextFile(String name) {
        super(name);
    }

    public TextFile() {
    }

    @FormattedText
    @EditorWidget(FreeTextPropertyWidget.class)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
