/*
 *
 * Date: 2011-feb-07
 * Author: davidw
 *
 */
package net.sf.xapp.uifwk;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SwingUtils
{

    public static Font DEFAULT_FONT = Font.decode("dialog-10");
    public static ImageIcon DEFAULT_FRAME_ICON;
    public static int MAX_CREATE_COMMANDS_IN_POP_UP = 3;

    public static void setFont(Container container)
    {
        setFont(container, DEFAULT_FONT);
    }

    public static void setFont(Container container, String fontStr)
    {
        setFont(container, Font.decode(fontStr));
    }
    public static void setFont(Container container, Font font)
    {
        container.setFont(font);
        trySetBorderFont(font, container);
        Component[] components = container.getComponents();
        for (Component component : components)
        {
            component.setFont(font);
            if (component instanceof Container)
            {
                setFont((Container) component, font);
            }
            trySetBorderFont(font, component);
            if(component instanceof JTable)
            {
                JTable table = (JTable) component;
                table.getTableHeader().setFont(font);
            }
        }

        if (container instanceof JMenu)
        {
            JMenu jMenu = (JMenu) container;
            for (int i = 0; i < jMenu.getItemCount(); i++)
            {
                JMenuItem mi = jMenu.getItem(i);
                mi.setFont(font);
            }
        }
    }

    private static void trySetBorderFont(Font font, Component component)
    {
        if (component instanceof JComponent)
        {
            JComponent jc = (JComponent) component;
            if (jc.getBorder() instanceof TitledBorder)
            {
                TitledBorder titledBorder = (TitledBorder) jc.getBorder();
                titledBorder.setTitleFont(font);
            }
        }
    }

    public static JFrame showInFrame(Container content)
    {
        JFrame jf = createFrame(content);
        jf.setVisible(true);
        return jf;
    }

    public static JFrame createFrame(Container content)
    {
        JFrame jf = new JFrame();
        jf.setContentPane(content);
        jf.pack();
        if (DEFAULT_FRAME_ICON != null)
        {
            jf.setIconImage(DEFAULT_FRAME_ICON.getImage());
        }
        return jf;
    }

    public static boolean askUser(Component parent, String question)
    {
        int i = JOptionPane.showOptionDialog(parent,
                question, "Question",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        return i == JOptionPane.YES_OPTION;
    }

    public static Box createHorizBox(Component... comps)
    {
        return createBox(BoxLayout.LINE_AXIS, comps);
    }

    public static Box createVertBox(Component... comps)
    {
        return createBox(BoxLayout.PAGE_AXIS, comps);
    }

    public static Box createBox(int axis, Component... comps)
    {
        Box r = new Box(axis);
        for (Component comp : comps)
        {
            r.add(comp);
        }
        return r;
    }

    public static void warnUser(Component parent, String message)
    {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static void setComponentSize(int width, int height, JComponent l)
    {
        l.setPreferredSize(new Dimension(width, height));
        l.setMinimumSize(new Dimension(width, height));
        if (l instanceof JTextField || l instanceof JComboBox)
        {
            l.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
        }
    }

    public static void invokeAfter(int delayInMillis, final Runnable task) {
        Timer timer = new Timer(delayInMillis, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                task.run();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}
