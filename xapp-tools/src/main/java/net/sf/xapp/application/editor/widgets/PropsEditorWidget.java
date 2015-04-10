package net.sf.xapp.application.editor.widgets;

import net.sf.xapp.marshalling.stringserializers.StringMapSerializer;
import net.sf.xapp.objectmodelling.core.ContainerProperty;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Created by oldDave on 05/03/2015.
 */
public class PropsEditorWidget extends AbstractPropertyWidget<Map<?, String>> {

    private PropsEditor textEditor;
    private JScrollPane scrollPane;

    @Override
    public JComponent getComponent() {
        if(textEditor == null ) {
            textEditor = new PropsEditor();
            scrollPane = new JScrollPane(textEditor);
            scrollPane.setPreferredSize(new Dimension(200,200));
        }

        return scrollPane;
    }

    @Override
    public Map<?, String> getValue() {
        return StringMapSerializer._read(((ContainerProperty) getProperty()).getMapKeyType(), textEditor.getText());
    }

    @Override
    public void setValue(Map<?, String> value, ObjectMeta target) {
        textEditor.setText(StringMapSerializer._write(value));
        textEditor.setFont(Font.decode("Courier-12"));
    }

    @Override
    public void setEditable(boolean editable) {

    }
}
