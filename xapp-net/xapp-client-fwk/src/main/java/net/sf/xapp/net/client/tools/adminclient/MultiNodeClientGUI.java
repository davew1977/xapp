/*
 *
 * Date: 2010-mar-04
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import net.sf.xapp.application.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiNodeClientGUI<T> extends JSplitPane
{
    private MultiNodeClientGUIListener<T> listener;
    private Map<T, ConsolePanel> consoles;
    private List<JComponent> singleNodeGUIS;
    private List<T> sources;

    public MultiNodeClientGUI()
    {
        super(JSplitPane.HORIZONTAL_SPLIT);
    }

    public void init(List<T> sources)
    {
        this.sources = sources;
        consoles = new HashMap<T, ConsolePanel>();
        singleNodeGUIS = new ArrayList<JComponent>();
        //setPreferredSize(new Dimension(1000,700));
        JSplitPane[] leftAndRight = new JSplitPane[]{new JSplitPane(JSplitPane.VERTICAL_SPLIT),
                new JSplitPane(JSplitPane.VERTICAL_SPLIT)};
        for (int i = 0; i < sources.size(); i++)
        {
            T t = sources.get(i);
            Box box = Box.createVerticalBox();
            ConsolePanel console = createConsole();
            consoles.put(t, console);
            JScrollPane jsp = new JScrollPane(console);
            jsp.setMinimumSize(new Dimension(600,300));
            jsp.setPreferredSize(new Dimension(600, 300));
            jsp.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
            box.add(jsp);
            //box.add(Box.createVerticalGlue());
            box.add(createInputTF(t));
            box.add(Box.createVerticalStrut(3));
            box.setBorder(BorderFactory.createTitledBorder(String.valueOf(t)));
            singleNodeGUIS.add(box);
            leftAndRight[i/2].add(box);
        }
        add(leftAndRight[0]);
        add(leftAndRight[1]);
    }

    public void setListener(MultiNodeClientGUIListener<T> listener)
    {
        this.listener = listener;
    }

    public void print(T src, String text)
    {
        if (consoles !=null)
        {
            consoles.get(src).append(text);
        }

    }
    public void println(T src, String text)
    {
        print(src, text  + "\n");
    }

    private CommandLineTF createInputTF(final T src)
    {
        final CommandLineTF tf = new CommandLineTF(20);

        SwingUtils.setComponentSize(100, 20, tf);
        tf.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                println(src, tf.getText());
                listener.newMessageTyped(src, tf.getText());
            }
        });
        return tf;
    }

    private ConsolePanel createConsole()
    {
        return new ConsolePanel(1000);
    }

    public JComponent getFirst()
    {
        return singleNodeGUIS.get(0);
    }

    public List<T> getSources()
    {
        return sources;
    }
}