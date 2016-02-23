package net.sf.xapp.application.editor.widgets;

import net.sf.xapp.marshalling.stringserializers.EnumListSerializer;
import net.sf.xapp.marshalling.stringserializers.StringMapSerializer;
import net.sf.xapp.objectmodelling.core.ContainerProperty;
import net.sf.xapp.objectmodelling.core.ObjectMeta;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;

/**
 * Created by oldDave on 05/03/2015.
 */
public class PropsEditorWidget extends AbstractPropertyWidget<Map<?, ?>> implements PropsProvider {

    private PropsEditor textEditor;
    private JComponent rootPane;

    @Override
    public JComponent getComponent() {
        if(textEditor == null ) {

            textEditor = new PropsEditor();
            JScrollPane scrollPane = new JScrollPane(textEditor);
            scrollPane.setPreferredSize(new Dimension(200, 200));
            if(getMapKeyType().isEnum()) {

                textEditor.setPropsProvider(this);
                rootPane = new Box(BoxLayout.PAGE_AXIS);
                rootPane.add(scrollPane);
                JLabel h = new JLabel("control-SPACE for key help");
                h.setPreferredSize(new Dimension(200, 15));
                h.setFont(h.getFont().deriveFont(9f));
                rootPane.add(h);
            } else {
                rootPane = scrollPane;
            }

        }

        return rootPane;
    }

    @Override
    public Map<?, ?> getValue() {
        return StringMapSerializer._read(getMapKeyType(), getMapValueType(), textEditor.getText());
    }

    private Type getMapValueType() {
        return ((ContainerProperty) getProperty()).getMapValueType();
    }

    private Class getMapKeyType() {
        return ((ContainerProperty) getProperty()).getMapKeyType();
    }

    @Override
    public void setValue(Map<?, ?> value, ObjectMeta target) {
        textEditor.setText(StringMapSerializer._write(value));
        textEditor.setFont(Font.decode("Courier-12"));
    }

    @Override
    public void setEditable(boolean editable) {

    }

    @Override
    public List<String> getKeys() {
        Enum[] enumValues = EnumListSerializer.getEnumValues(getMapKeyType());
        java.util.List<String> options = new ArrayList<>();
        for (Enum enumValue : enumValues) {
            options.add(enumValue.name());
        }
        return options;
    }

    @Override
    public List<String> getValues(String key) {
        return new ArrayList<>();
    }
}
