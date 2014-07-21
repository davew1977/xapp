package net.sf.xapp.codegen.editor;

import net.sf.xapp.application.utils.SwingUtils;
import net.sf.xapp.codegen.model.Artifact;
import net.sf.xapp.codegen.model.Model;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: davidw
 * Date: 1/15/14
 * Time: 7:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class SearchPane extends JPanel {
    private final JTextField textField;
    private final JLabel resultCount;

    public SearchPane(final ActionListener listener, final Model model) {
        setSize(150, 30);
        add(new JLabel("Search"));
        textField = new JTextField("");
        textField.setPreferredSize(new Dimension(150,20));
        resultCount = new JLabel("hits: ");
        add(textField);
        add(resultCount);
        textField.addCaretListener(new CaretListener()
        {
            @Override
            public void caretUpdate(CaretEvent e)
            {
                String text = textField.getText();
                List<Artifact> artifacts = model.search(text);
                resultCount.setText("hits: " + artifacts.size());
                listener.actionPerformed(new ActionEvent(artifacts, 1, ""));
            }
        });
        SwingUtils.setFont(this, Font.decode("Tahoma-BOLD-10"));
    }
}
